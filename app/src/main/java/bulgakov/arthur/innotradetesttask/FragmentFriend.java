package bulgakov.arthur.innotradetesttask;

import android.content.DialogInterface;
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
import android.widget.Toast;

import com.github.gorbin.asne.core.SocialNetwork;
import com.github.gorbin.asne.core.SocialNetworkManager;
import com.github.gorbin.asne.core.listener.OnRequestDetailedSocialPersonCompleteListener;
import com.github.gorbin.asne.core.listener.OnRequestSocialPersonCompleteListener;
import com.github.gorbin.asne.core.persons.SocialPerson;
import com.github.gorbin.asne.vk.VkSocialNetwork;

import bulgakov.arthur.innotradetesttask.utils.ADialogs;
import bulgakov.arthur.innotradetesttask.utils.SocialCard;
import bulgakov.arthur.innotradetesttask.utils.VkConstants;

/**
 *
 */
public class FragmentFriend extends Fragment implements OnRequestDetailedSocialPersonCompleteListener, OnRequestSocialPersonCompleteListener, SocialNetworkManager.OnInitializationCompleteListener {

   public static final String FRAGMENT_FRIEND_TAG = "friend";

   private SocialCard vkSocialCard;
   private SocialNetworkManager socialNetworkManager;
   private boolean detailsIsShown;
   private String userId;
   private ADialogs loadingDialog;

   public FragmentFriend() {
   }

   public static FragmentFriend newInstance(String userId) {
      FragmentFriend fragment = new FragmentFriend();
      Bundle args = new Bundle();
      args.putString(VkConstants.USER_ID, userId);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      Log.d(ActivityMain.APP_TAG, "Friend Fragment onCreateView");
      setRetainInstance(true);
      userId = getArguments().getString(VkConstants.USER_ID);
      setHasOptionsMenu(true);
      loadingDialog = new ADialogs(getActivity());
      loadingDialog.customProgressDialog(true, "Loading profile...", null);
      ((ActivityMain) getActivity()).getSupportActionBar().setTitle(getString(R.string.friend_title));

      int darkColor = getResources().getColor(VkConstants.color_light);
      int textColor = getResources().getColor(VkConstants.color);
      int color = getResources().getColor(VkConstants.color);
      int image = VkConstants.userPhoto;
      View rootView = inflater.inflate(R.layout.fragment_friend, container, false);
      vkSocialCard = (SocialCard) rootView.findViewById(R.id.info_card);
      vkSocialCard.setColors(color, textColor, darkColor);
      vkSocialCard.setImageResource(image);

      initSNM();
      updateInfoCard();

      return rootView;
   }

   private void initSNM() {
      FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
      socialNetworkManager = (SocialNetworkManager) fragmentManager
              .findFragmentByTag(FragmentLogin.SOCIAL_NETWORK_TAG);

      if (!socialNetworkManager.getInitializedSocialNetworks().isEmpty()) {
         for (SocialNetwork socialNetwork : socialNetworkManager.getInitializedSocialNetworks()) {
            socialNetwork.setOnRequestDetailedSocialPersonCompleteListener(this);
            socialNetwork.setOnRequestSocialPersonCompleteListener(this);
         }
         updateInfoCard();
         Log.d(ActivityMain.APP_TAG, "update Info Card from onCreateView");
      }
      socialNetworkManager.setOnInitializationCompleteListener(this);
   }

   @Override
   public void onStop() {
      super.onStop();
      Log.d(ActivityMain.APP_TAG, "FragmentLogin onStop");
      socialNetworkManager.setOnInitializationCompleteListener(null);
   }

   @Override
   public void onSocialNetworkManagerInitialized() {
      Log.d(ActivityMain.APP_TAG, "snm is initialized");
      for (SocialNetwork socialNetwork : socialNetworkManager.getInitializedSocialNetworks()) {
         socialNetwork.setOnRequestCurrentPersonCompleteListener(this);
         socialNetwork.setOnRequestDetailedSocialPersonCompleteListener(this);
      }
      updateInfoCard();
      Log.d(ActivityMain.APP_TAG, "update Info Card from onSocialNetworkManagerInitialized");
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.friend_menu, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      return super.onOptionsItemSelected(item);
   }


   @Override
   public void onRequestSocialPersonSuccess(int socialNetworkId, SocialPerson socialPerson) {
      setSocialCardFromUser(socialPerson, vkSocialCard);
   }

   @Override
   public void onRequestDetailedSocialPersonSuccess(int id, SocialPerson socialPerson) {
      setSocialCardFromUser(socialPerson, vkSocialCard);
   }

   @Override
   public void onError(int i, String s, String s1, Object data) {
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

   private void updateInfoCard() {
//      if (socialNetwork.isConnected()) {
      vkSocialCard.connect.setVisibility(View.GONE);
      vkSocialCard.friends.setVisibility(View.VISIBLE);
      vkSocialCard.friends.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            FragmentFriendsList friends = FragmentFriendsList.newInstannce(userId);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, friends, FRAGMENT_FRIEND_TAG)
                    .commit();
         }
      });
      vkSocialCard.detail.setVisibility(View.VISIBLE);
      vkSocialCard.detail.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            detailsIsShown = !detailsIsShown;
            socialOrDetailed();
         }
      });
      socialOrDetailed();
//      } else {
//         vkSocialCard.detail.setVisibility(View.GONE);
//         vkSocialCard.connect.setVisibility(View.GONE);
//         vkSocialCard.friends.setVisibility(View.GONE);
//         defaultSocialCardData();
//      }
   }

   private void socialOrDetailed() {
      SocialNetwork vkSocialNetwork = socialNetworkManager.getSocialNetwork(VkSocialNetwork.ID);
      loadingDialog.showProgress();
      if (detailsIsShown) {
         vkSocialCard.detail.setText("hide details");
         vkSocialNetwork.requestDetailedSocialPerson(userId);
      } else {
         vkSocialCard.detail.setText("show details...");
         vkSocialNetwork.requestSocialPerson(userId);
      }
   }

   private void defaultSocialCardData() {
      vkSocialCard.setName("NoName");
      vkSocialCard.setId("unknown");
      vkSocialCard.setImageResource(VkConstants.userPhoto);
   }

   public void setSocialCardFromUser(SocialPerson socialPerson, SocialCard socialCard) {
      socialCard.setName(socialPerson.name);
      String detailedSocialPersonString = socialPerson.toString();
      String infoString = detailedSocialPersonString.substring(detailedSocialPersonString.indexOf("{") + 1, detailedSocialPersonString.lastIndexOf("}"));
      socialCard.setId(infoString.replace(", ", "\n"));
      socialCard.setImage(socialPerson.avatarURL, VkConstants.userPhoto, R.drawable.error);
      loadingDialog.cancelProgress();
   }
}