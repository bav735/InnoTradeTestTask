package bulgakov.arthur.innotradetesttask.utils;

import bulgakov.arthur.innotradetesttask.R;

/**
 * Contains VK constants and static methods
 */
public class VkConstants {

   public static final String VK_KEY = "4935141";
   public static final String USER_ID = "USER_ID";
   public static final int logo = R.drawable.ic_vk;
   public static final String socialName = "Vkontakte";
   public static final int userPhoto = R.drawable.vk_user;
   public static final int color = R.color.vk;
   public static final int color_light = R.color.vk_light;

   public static String handleError(int socialNetworkID, String requestID, String errorMessage) {
      return "ERROR: " + errorMessage + " by " + requestID;
   }
}