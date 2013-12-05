package it.backbox.gui;

import it.backbox.bean.File;
import it.backbox.db.DBAndroidManager;
import it.backbox.exception.BackBoxException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String stPathToDB;
		try {
			stPathToDB = Environment.getExternalStorageDirectory().getCanonicalPath() + "/backbox/backbox.db.temp";
			DBAndroidManager dbm = new DBAndroidManager(stPathToDB);
			dbm.openDB();
			List<File> files = dbm.getAllFiles();
			
			final ListView listview = (ListView) findViewById(R.id.listview);
			final ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, android.R.layout.simple_list_item_1, files);
			listview.setAdapter(adapter);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BackBoxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
