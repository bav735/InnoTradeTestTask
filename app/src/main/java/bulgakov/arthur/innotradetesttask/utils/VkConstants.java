package bulgakov.arthur.innotradetesttask.utils;

import bulgakov.arthur.innotradetesttask.R;

public class VkConstants {

   public static final String VK_KEY = "4935141";
   public static final String USER_ID = "USER_ID";
   public static final int logo = R.drawable.ic_vk;
   public static final String socialName = "Vkontakte";
   public static final int userPhoto = R.drawable.vk_user;
   public static final int color = R.color.vk;
   public static final int color_light = R.color.vk_light;
   public static final String[] vkShare = {"Post message", "Post photo to wall", "Post Link"};
   public static final SharePost[] vkShareNum = {SharePost.POST_MESSAGE, SharePost.POST_PHOTO,
           SharePost.POST_LINK};

   public static String handleError(int socialNetworkID, String requestID, String errorMessage) {
      return "ERROR: " + errorMessage + " by " + requestID;
   }

   public enum SharePost {
      POST_MESSAGE,
      POST_PHOTO,
      POST_LINK,
   }
}