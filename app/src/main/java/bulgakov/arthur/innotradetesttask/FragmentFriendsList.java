package bulgakov.arthur.innotradetesttask;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.gorbin.asne.core.persons.SocialPerson;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import bulgakov.arthur.innotradetesttask.utils.ADialogs;
import bulgakov.arthur.innotradetesttask.utils.VkConstants;

/**
 *
 */
public class FragmentFriendsList extends Fragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

   public static final String FRAGMENT_FRIEND_LIST_TAG = "friend_list";

   private String userId;
   private ListView listView;
   private FriendsListAdapter adapter;
   private ArrayList<SocialPerson> friendsList = new ArrayList<>();
   private ArrayList<SocialPerson> searchedFriendsList = new ArrayList<>();
   private ADialogs loadingDialog;

   public FragmentFriendsList() {
   }

   public static FragmentFriendsList newInstannce(String userId) {
      FragmentFriendsList fragment = new FragmentFriendsList();
      Bundle args = new Bundle();
      args.putString(VkConstants.USER_ID, userId);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      setRetainInstance(true);
      userId = getArguments().getString(VkConstants.USER_ID);
      Log.d(ActivityMain.APP_TAG, "user id = " + userId);
      View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);
      listView = (ListView) rootView.findViewById(R.id.list_view);

      loadingDialog = new ADialogs(getActivity());
      loadingDialog.customProgressDialog(true, "Loading friends...", null);

      loadingDialog.showProgress();
      final VKRequest requestFriends = new VKRequest("friends.get", VKParameters.from(VKApiConst.USER_ID,
              userId, VKApiConst.FIELDS, "id,first_name,last_name,photo_max_orig"));
      requestFriends.executeWithListener(new VKRequest.VKRequestListener() {
         @Override
         public void onComplete(VKResponse responseFriends) {
            Log.d(ActivityMain.APP_TAG, "getting ids");
            JSONObject jsonResponse;
            SocialPerson socialPerson = new SocialPerson();
            friendsList = new ArrayList<>();
            try {
               jsonResponse = responseFriends.json.getJSONObject("response");
               JSONArray jsonArray = jsonResponse.getJSONArray("items");
               for (int i = 0; i < jsonArray.length(); i++) {
                  getSocialPerson(socialPerson, jsonArray.getJSONObject(i));
                  friendsList.add(socialPerson);
                  socialPerson = new SocialPerson();
               }
               searchedFriendsList = friendsList;
               initList();
               ((ActivityMain) getActivity()).getSupportActionBar().setTitle(friendsList.size() + " Friends");
            } catch (JSONException e) {
               Log.d(ActivityMain.APP_TAG, e.toString());
            }
            loadingDialog.cancelProgress();
         }

         @Override
         public void onError(VKError error) {
            Log.d(ActivityMain.APP_TAG, error.toString());
            loadingDialog.cancelProgress();
            new ADialogs(getActivity()).customErrorDialog(error.toString(), new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                  getActivity().getSupportFragmentManager().popBackStackImmediate();
               }
            });
         }

         @Override
         public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            Log.d(ActivityMain.APP_TAG, "get friends fail attempt # " + attemptNumber);
            Toast.makeText(getActivity(), "Error while getting friends, fail attempt # " + attemptNumber
                    , Toast.LENGTH_SHORT).show();
            loadingDialog.cancelProgress();
         }
      });

      setHasOptionsMenu(true);
      return rootView;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.friends_list_menu, menu);
      MenuItem searchItem = menu.findItem(R.id.action_search);
      SearchView searchView = (SearchView) searchItem.getActionView();
      searchView.setOnQueryTextListener(this);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      return super.onOptionsItemSelected(item);
   }

   @Override
   public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      FragmentFriend clickedFriend = FragmentFriend
              .newInstance(searchedFriendsList.get(i).id);
      Log.d(ActivityMain.APP_TAG, "clicked on " + searchedFriendsList.get(i).id);
      getActivity().getSupportFragmentManager().beginTransaction()
              .addToBackStack(null)
              .replace(R.id.fragment_container, clickedFriend, FRAGMENT_FRIEND_LIST_TAG)
              .commit();
   }

   private SocialPerson getSocialPerson(SocialPerson socialPerson, JSONObject jsonResponse) throws JSONException {
      String firstName = null;
      String lastName = null;
      if (jsonResponse.has("id")) {
         socialPerson.id = jsonResponse.getString("id");
         socialPerson.profileURL = "http://vk.com/id" + jsonResponse.getString("id");
      }
      if (jsonResponse.has("first_name")) {
         firstName = jsonResponse.getString("first_name");
      }
      if (jsonResponse.has("last_name")) {
         lastName = jsonResponse.getString("last_name");
      }
      socialPerson.name = firstName + " " + lastName;
      if (jsonResponse.has("photo_max_orig")) {
         socialPerson.avatarURL = jsonResponse.getString("photo_max_orig");
      }
      return socialPerson;
   }

   @Override
   public boolean onQueryTextSubmit(final String string) {
      return false;
   }

   @Override
   public boolean onQueryTextChange(final String string) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            searchedFriendsList = new ArrayList<>();
            for (SocialPerson socialPerson : friendsList) {
               if (socialPerson.name.toLowerCase().contains(string.toLowerCase()))
                  searchedFriendsList.add(socialPerson);
            }
            getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  initList();
               }
            });
         }
      }).start();
      return false;
   }

   private void initList() {
      adapter = new FriendsListAdapter(getActivity(), searchedFriendsList);
      listView.setAdapter(adapter);
      listView.setOnItemClickListener(this);
   }
}