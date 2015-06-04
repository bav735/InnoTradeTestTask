package bulgakov.arthur.innotradetesttask;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkException;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnLoginCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestDetailedSocialPersonCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.vk.VkSocialNetwork;
import com.vk.sdk.VKScope;

import bulgakov.arthur.innotradetesttask.utils.ADialogs;
import bulgakov.arthur.innotradetesttask.utils.SocialCard;
import bulgakov.arthur.innotradetesttask.utils.VkConstants;

/**
 * Implements authorization to VK
 * Requests general or detailed user information
 * Catches errors during requests to VK, shows error dialog
 * Redirects to FragmentFriendsList on friends button click
 */
public class FragmentLogin extends Fragment
        implements OnLoginCompleteListener, SocialNetworkManager.OnInitializationCompleteListener,
        OnRequestSocialPersonCompleteListener, OnRequestDetailedSocialPersonCompleteListener {

   public static final String SOCIAL_NETWORK_TAG = "SocialIntegrationMain.SOCIAL_NETWORK_TAG";
   public static final String FRAGMENT_LOGIN_TAG = "login";

   public static SocialNetworkManager socialNetworkManager;
   private SocialNetwork vkSocialNetwork;
   private FragmentManager fragmentManager;
   private ADialogs loginProgressDialog;
   private ADialogs loadingDialog;
   private SocialCard vkSocialCard;
   private String userId;
   private boolean isDetailedInfoShown = true;
   private boolean isUpdatedOnCreateView = false;

   public FragmentLogin() {
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      setRetainInstance(true);
      setHasOptionsMenu(true);
      Log.d(ActivityMain.APP_TAG, "Login Fragment onCreate View");
      fragmentManager = getActivity().getSupportFragmentManager();
      View rootView = inflater.inflate(R.layout.fragment_login, container, false);
      ((ActivityMain) getActivity()).getSupportActionBar().setTitle(R.string.my_auth_title);

      loginProgressDialog = new ADialogs(getActivity());
      loginProgressDialog.customProgressDialog(true, getString(R.string.logging_in), null);
      loadingDialog = new ADialogs(getActivity());
      loadingDialog.customProgressDialog(true, getString(R.string.loading_profile), null);

      vkSocialCard = (SocialCard) rootView.findViewById(R.id.vk_card);
      initializeSocialNetworkManager();

      return rootView;
   }

   private void initializeSocialNetworkManager() {
      String[] vkScope = new String[]{
              VKScope.FRIENDS,
              VKScope.WALL,
              VKScope.PHOTOS,
              VKScope.NOHTTPS,
              VKScope.STATUS,
      };

      socialNetworkManager = (SocialNetworkManager) fragmentManager.findFragmentByTag(SOCIAL_NETWORK_TAG);

      if (socialNetworkManager == null) {
         socialNetworkManager = new SocialNetworkManager();

         vkSocialNetwork = new VkSocialNetwork(this, VkConstants.VK_KEY, vkScope);
         socialNetworkManager.addSocialNetwork(vkSocialNetwork);

         fragmentManager.beginTransaction()
                 .add(socialNetworkManager, SOCIAL_NETWORK_TAG)
                 .commit();
      } else {
         Log.d(ActivityMain.APP_TAG, "snm is not null");
         if (!socialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
            for (SocialNetwork socialNetwork : socialNetworkManager.getInitializedSocialNetworks()) {
               socialNetwork.setOnRequestCurrentPersonCompleteListener(this);
               socialNetwork.setOnRequestDetailedSocialPersonCompleteListener(this);
               socialNetwork.setOnLoginCompleteListener(this);
            }
            isUpdatedOnCreateView = true;
            updateVkSocialCard();
            Log.d(ActivityMain.APP_TAG, "updateVkSocialCard from onCreateView");
         }
      }
      socialNetworkManager.setOnInitializationCompleteListener(this);
   }

   private void setDefaultUserInfo() {
      vkSocialCard.detail.setVisibility(View.GONE);
      vkSocialCard.setConnectButtonIcon(VkConstants.logo);
      vkSocialCard.setConnectButtonText(getString(R.string.vk_login));
      vkSocialCard.connect.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            loginProgressDialog.showProgress();
            if (vkSocialNetwork.isConnected())
               vkSocialNetwork.logout();
            vkSocialNetwork.requestLogin();
         }
      });
      vkSocialCard.friends.setVisibility(View.GONE);
      userId = null;
      vkSocialCard.setName(getString(R.string.vk_def_name));
      vkSocialCard.setId(getString(R.string.vk_def_id));
      vkSocialCard.setImageResource(VkConstants.userPhoto);
   }

   @Override
   public void onStop() {
      super.onStop();
      Log.d(ActivityMain.APP_TAG, "FragmentLogin onStop");
      socialNetworkManager.setOnInitializationCompleteListener(null);
   }

   @Override
   public void onSocialNetworkManagerInitialized() {
      if (isUpdatedOnCreateView)
         return;
      Log.d(ActivityMain.APP_TAG, "snm is initialized");
      for (SocialNetwork socialNetwork : socialNetworkManager.getInitializedSocialNetworks()) {
         socialNetwork.setOnRequestCurrentPersonCompleteListener(this);
         socialNetwork.setOnRequestDetailedSocialPersonCompleteListener(this);
         socialNetwork.setOnLoginCompleteListener(this);
      }
      updateVkSocialCard();
      Log.d(ActivityMain.APP_TAG, "updateVkSocialCard from onSocialNetworkManagerInitialized");
   }

   @Override
   public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
      isDetailedInfoShown = false;
      vkSocialCard.detail.setText(getString(R.string.show_details));
      setSocialCardFromUser(socialPerson, vkSocialCard);
   }

   @Override
   public void onRequestDetailedSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
      isDetailedInfoShown = true;
      vkSocialCard.detail.setText(getString(R.string.hide_details));
      setSocialCardFromUser(socialPerson, vkSocialCard);
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
   }

   private void updateVkSocialCard() {
      vkSocialNetwork = socialNetworkManager.getSocialNetwork(VkSocialNetwork.ID);
      if ((vkSocialNetwork != null) && (vkSocialNetwork.isConnected())) {
         Log.d(ActivityMain.APP_TAG, "vk is not null");
         vkSocialCard.setConnectButtonIcon(VkConstants.logo);
         vkSocialCard.setConnectButtonText(getString(R.string.vk_logout));
         vkSocialCard.connect.setVisibility(View.VISIBLE);
         vkSocialCard.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               vkSocialNetwork.logout();
               updateVkSocialCard();
            }
         });
         vkSocialCard.friends.setVisibility(View.VISIBLE);
         vkSocialCard.friends.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
               Log.d(ActivityMain.APP_TAG, "friends->");
               fragmentManager.beginTransaction()
                       .addToBackStack(null)
                       .replace(R.id.fragment_container, FragmentFriendsList.newInstance(userId),
                               FragmentFriendsList.FRAGMENT_FRIEND_LIST_TAG)
                       .commit();
            }
         });
         vkSocialCard.detail.setVisibility(View.VISIBLE);
         vkSocialCard.detail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
               if (isDetailedInfoShown)
                  getGeneralUserInfo();
               else
                  getDetailedUserInfo();
            }
         });

         if (isDetailedInfoShown)
            getDetailedUserInfo();
         else
            getGeneralUserInfo();
      } else {
         Log.d(ActivityMain.APP_TAG, "vk is null");
         setDefaultUserInfo();
      }
   }

   private void getGeneralUserInfo() {
      loadingDialog.showProgress();
      try {
         vkSocialNetwork.requestCurrentPerson();
      } catch (SocialNetworkException e) {
         e.printStackTrace();
      }
   }

   private void getDetailedUserInfo() {
      loadingDialog.showProgress();
      try {
         vkSocialNetwork.requestDetailedCurrentPerson();
      } catch (SocialNetworkException e) {
         e.printStackTrace();
      }
   }

   public void setSocialCardFromUser(SocialPerson socialPerson, SocialCard socialCard) {
      userId = socialPerson.id;
      socialCard.setName(socialPerson.name);
      String detailedSocialPersonString = socialPerson.toString();
      String infoString = detailedSocialPersonString.substring(detailedSocialPersonString.indexOf("{") + 1, detailedSocialPersonString.lastIndexOf("}"));
      socialCard.setId(infoString.replace(", ", "\n"));
      socialCard.setImage(socialPerson.avatarURL, VkConstants.userPhoto, R.drawable.error);
      loadingDialog.cancelProgress();
   }

   @Override
   public void onError(int i, String s, String s1, Object o) {
      Log.d(ActivityMain.APP_TAG, "login fragment err");
      if (loadingDialog != null) {
         loadingDialog.cancelProgress();
      }
      new ADialogs(getActivity()).customErrorDialog(VkConstants.handleError(i, s, s1),
              new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                    setDefaultUserInfo();
                 }
              });
   }

   @Override
   public void onLoginSuccess(int i) {
      Log.d(ActivityMain.APP_TAG, "login succ");
      updateVkSocialCard();
   }

   @Override
   public void onResume() {
      Log.d(ActivityMain.APP_TAG, "Login Fragment onResume");
      super.onResume();
      if (loginProgressDialog != null) {
         loginProgressDialog.cancelProgress();
      }
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.login_menu, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.login_refresh_action:
            updateVkSocialCard();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
