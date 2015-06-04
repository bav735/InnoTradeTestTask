package bulgakov.arthur.innotradetesttask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Starts LoginFragment on first create
 * Listens to fragments back stack
 */
public class ActivityMain extends ActionBarActivity {

   public static final String APP_TAG = "inno_trade_tag";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      Log.d(APP_TAG, "ActivityMain onCreate");
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      FragmentManager fragmentManager = getSupportFragmentManager();
      fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
         @Override
         public void onBackStackChanged() {
            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);
            if (fragment == null) {
               onBackPressed();
               return;
            }
            String fragmentTag = getFragmentTag(fragment);
            Log.d(APP_TAG, "onBackStackChanged - " + fragmentTag);
            if (!fragmentTag.equals(FragmentLogin.FRAGMENT_LOGIN_TAG)) {
               getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
               getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
         }
      });
      if (savedInstanceState == null) {
         fragmentManager.beginTransaction()
                 .addToBackStack(null)
                 .replace(R.id.fragment_container, new FragmentLogin(), FragmentLogin.FRAGMENT_LOGIN_TAG)
                 .commit();
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            getSupportFragmentManager().popBackStack();
            return true;
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentLogin.SOCIAL_NETWORK_TAG);
      if (fragment != null && resultCode == RESULT_OK) {
         fragment.onActivityResult(requestCode, resultCode, data);
      }
   }

   private String getFragmentTag(Fragment fragment) {
      if (fragment instanceof FragmentLogin)
         return FragmentLogin.FRAGMENT_LOGIN_TAG;
      if (fragment instanceof FragmentFriend)
         return FragmentFriend.FRAGMENT_FRIEND_TAG;
      if (fragment instanceof FragmentFriendsList)
         return FragmentFriendsList.FRAGMENT_FRIEND_LIST_TAG;
      return null;
   }
}