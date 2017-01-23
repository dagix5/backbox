package it.backbox.compare;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import it.backbox.IDBManager;
import it.backbox.bean.Folder;
import it.backbox.compare.CompareResult.Status;

public class FileCompare {
	private static final Logger _log = Logger.getLogger(FileCompare.class.getCanonicalName());

	private IDBManager dbm;
	private List<Folder> folders;
	private Set<String> exclusions;
	private String basePath;

	private Table<String, String, CompareResult> result;
	private Cache<String, List<it.backbox.bean.File>> cache;
	private Cache<String, String> hashCache;

	public FileCompare(IDBManager dbm, String basePath, List<Folder> folders, Set<String> exclusions) {
		this.dbm = dbm;
		this.folders = folders;
		this.exclusions = exclusions;
		this.basePath = basePath;

		result = HashBasedTable.create();
		cache = CacheBuilder.newBuilder().maximumSize(99999).build();
		hashCache = CacheBuilder.newBuilder().maximumSize(99999).build();
	}

	public Table<String, String, CompareResult> run() throws IOException, SQLException {
		for(Folder folder : folders) {
			final Path folderPath;
			if (basePath != null)
				folderPath = Paths.get(basePath).resolve(folder.getAlias());
			else
				folderPath = Paths.get(folder.getPath());
			Files.walkFileTree(folderPath, new FileVisitor<Path>() {
	
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					if (exclusions.contains(folderPath.relativize(dir).toString()))
						return FileVisitResult.SKIP_SUBTREE;
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
					String filename = FilenameUtils.separatorsToWindows(folderPath.relativize(filePath).toString());
					String hash;
					File file = filePath.toFile();
					
//					try {
//						List<it.backbox.bean.File> fs = dbm.getFileRecord(folder.getAlias(), filename);
//						if ((fs != null) && (fs.size() == 1)) {
//							it.backbox.bean.File f = fs.get(0);
//							if ((f != null) && (f.getTimestamp().compareTo(new Date(file.lastModified())) == 0)) {
//								if (_log.isLoggable(Level.FINE))
//									_log.log(Level.FINE,
//											"[" + folder.getAlias() + "] [" + filename + "] [] Found");
//								return FileVisitResult.CONTINUE;
//							}
//						}
//					} catch (SQLException e) {
//						if (_log.isLoggable(Level.SEVERE))
//							_log.log(Level.SEVERE, "[" + folder.getAlias() + "] [" + filename + "] [] Error accessing database", e);
//						return FileVisitResult.TERMINATE;
//					}
					
					try {
						hash = hashCache.getIfPresent(filePath.toAbsolutePath().toString());
						if (hash == null) {
							hash = DigestUtils.sha1Hex(new BufferedInputStream(new FileInputStream(file)));
							hashCache.put(filePath.toAbsolutePath().toString(), hash);
						}
					} catch (IOException e) {
						if (_log.isLoggable(Level.WARNING))
							_log.log(Level.WARNING,
									"[" + folder.getAlias() + "] [" + filename + "] [] Error calculating hash", e);
						return FileVisitResult.CONTINUE;
					}
	
					try {
						List<it.backbox.bean.File> files = cache.getIfPresent(hash);
						if (files == null) {
							files = dbm.getFiles(hash);
							cache.put(hash, files);
						}
	
						if ((files != null) && !files.isEmpty()) {
							for (it.backbox.bean.File ff : files) {
								if (ff.getFolderAlias().equals(folder.getAlias()) && ff.getFilename().equals(filename)) {
									if (_log.isLoggable(Level.FINE))
										_log.log(Level.FINE,
												"[" + folder.getAlias() + "] [" + filename + "] [" + hash + "] Found");
									return FileVisitResult.CONTINUE;
								}
							}
						}
	
						if (_log.isLoggable(Level.FINE))
							_log.log(Level.FINE,
									"[" + folder.getAlias() + "] [" + filename + "] [" + hash + "] Not found in DB");
	
						it.backbox.bean.File f = new it.backbox.bean.File();
						f.setFilename(filename);
						f.setFolderAlias(folder.getAlias());
						f.setHash(hash);
						f.setTimestamp(new Date(file.lastModified()));
						f.setSize(file.length());
						
						CompareResult cr = new CompareResult();
						cr.setFile(f);
						cr.setPath(folderPath);
						cr.setFolder(folder);
	
						if ((files != null) && !files.isEmpty()) {
							it.backbox.bean.File of = files.get(0);
							f.setChunks(of.getChunks());
							f.setCompressed(of.getCompressed());
							f.setEncrypted(of.getEncrypted());
							f.setSplitted(of.getSplitted());
							
							cr.setStatus(Status.COPIED);
						} else {
							cr.setStatus(Status.NEW);
						}
	
						result.put(f.getHash(), folder.getAlias() + '\\' + f.getFilename(), cr);
	
					} catch (SQLException e) {
						if (_log.isLoggable(Level.SEVERE))
							_log.log(Level.SEVERE, "[" + folder.getAlias() + "] [" + filename + "] [" + hash
									+ "] Error accessing database", e);
						return FileVisitResult.TERMINATE;
					}
	
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					String filename = folderPath.relativize(file).toString();
					if (_log.isLoggable(Level.WARNING))
						_log.log(Level.WARNING, "[" + folder.getAlias() + "] [" + filename + "] [] Error visiting file " + file.toString(), exc);
	
					return FileVisitResult.CONTINUE;
				}
	
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
	
			});
			
			List<it.backbox.bean.File> filesDB = dbm.getFilesInFolder(folder.getAlias());
			for (it.backbox.bean.File fileDB : filesDB) {
				Path filePath = Paths.get(folderPath.toString(), FilenameUtils.separatorsToSystem(fileDB.getFilename()));
				if (Files.exists(filePath)) {
					File file = filePath.toFile();
					String hash = hashCache.getIfPresent(filePath.toAbsolutePath().toString());
					if (hash == null) {
						hash = DigestUtils.sha1Hex(new BufferedInputStream(new FileInputStream(file)));
						hashCache.put(filePath.toAbsolutePath().toString(), hash);
					}
					if (hash.equals(fileDB.getHash())) {
						if (_log.isLoggable(Level.FINE))
							_log.log(Level.FINE, "[" + folder.getAlias() + "] [" + fileDB.getFilename() + "] ["
									+ fileDB.getHash() + "] Found");
						continue;
					}
					if (_log.isLoggable(Level.FINE))
						_log.log(Level.FINE, "[" + folder.getAlias() + "] [" + fileDB.getFilename() + "] ["
								+ fileDB.getHash() + "] Different");
				}
	
				if (_log.isLoggable(Level.FINE))
					_log.log(Level.FINE, "[" + folder.getAlias() + "] [" + fileDB.getFilename() + "] [" + fileDB.getHash()
							+ "] Not found in file system");
	
				CompareResult cr = new CompareResult(fileDB, Status.DELETED);
				cr.setFolder(folder);
				result.put(fileDB.getHash(), folder.getAlias() + '\\' + fileDB.getFilename(), cr);
			}
		}
		return result;
	}

}
