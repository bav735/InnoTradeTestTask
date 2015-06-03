package bulgakov.arthur.innotradetesttask.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import bulgakov.arthur.innotradetesttask.R;


public class ADialogs {
   private Context context;
   private ADialogsListener aListener = null;
   private ADialogsEditListener aEditListener = null;
   private ADialogsProgressListener progressListener = null;
   private ADialogsListListener listListener = null;
   // ProgressDialog
   private ProgressDialog pd;
   // CustomListDialog
   private ADialogsImageAlertListener imageAlertListener = null;

   public ADialogs(Context context) {
      this.context = context;
   }

   private AlertDialog.Builder build(boolean cancelable, String title) {
      return build(cancelable, title, null);
   }

   private AlertDialog.Builder build(boolean cancelable, String title, String message) {
      AlertDialog.Builder ad = new AlertDialog.Builder(context);
      if (title != null) {
         ad.setTitle(title);
      }
      if (message != null) {
         ad.setMessage(message);
      }
      if (cancelable) {
         ad.setCancelable(true);
      }
      return ad;
   }

   public void setADialogsListener(ADialogsListener aListener) {
      this.aListener = aListener;
   }

   public void alert(boolean cancelable, String title, String message, String positiveButton, String negativeButton) {
      AlertDialog.Builder ad = build(cancelable, title, message);
      if (positiveButton != null) {
         ad.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               if (aListener != null) {
                  aListener.onADialogsPositiveClick(dialog);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      if (negativeButton != null) {
         ad.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               if (aListener != null) {
                  aListener.onADialogsNegativeClick(dialog);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      if (cancelable) {
         ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
               if (aListener != null) {
                  aListener.onADialogsCancel(dialog);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      ad.create().show();
   }

   public void setADialogsListListener(ADialogsListListener listListener) {
      this.listListener = listListener;
   }

   public void listDialog(boolean cancelable, String title, String[] list, final VkConstants.SharePost[] numList) {
      AlertDialog.Builder ad = build(cancelable, title, null);

      final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
              context,
              android.R.layout.select_dialog_item, list);

      ad.setNegativeButton("Cancel",
              new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                 }
              });

      ad.setAdapter(arrayAdapter,
              new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int id) {
                    if (listListener != null) {
                       listListener.onADialogsListDialog(dialog, id, numList[id]);
                    } else {
                       dialog.cancel();
                    }
                 }
              });
      if (cancelable) {
         ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
               dialog.cancel();
            }
         });
      }
      ad.create().show();
   }

   public void setADialogsEditListener(ADialogsEditListener aEditListener) {
      this.aEditListener = aEditListener;
   }

   public void customEditDialog(boolean cancelable, String title, String positiveButton, String negativeButton) {
      LayoutInflater li = LayoutInflater.from(context);
      View customView = li.inflate(R.layout.edit_text_dialog, null);
      AlertDialog.Builder ad = build(cancelable, title);
      ad.setView(customView);
      final EditText userInput = (EditText) customView.findViewById(R.id.editTextDialogUserInput);
      if (positiveButton != null) {
         ad.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               if (aEditListener != null) {
                  aEditListener.onADialogsPositiveClick(dialog, userInput.getText().toString());
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      if (negativeButton != null) {
         ad.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               if (aEditListener != null) {
                  aEditListener.onADialogsNegativeClick(dialog);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      if (cancelable) {
         ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
               if (aEditListener != null) {
                  aEditListener.onADialogsCancel(dialog);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      ad.create().show();
   }

   public void setADialogsProgressListener(ADialogsProgressListener progressListener) {
      this.progressListener = progressListener;
   }

   public void customProgressDialog(boolean cancelable, String message, DialogInterface.OnClickListener listener) {
      pd = new ProgressDialog(context);
      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
      pd.setMessage(message);
      pd.setCancelable(cancelable);
      pd.setCanceledOnTouchOutside(cancelable);
      if (cancelable) {
         pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
               if (progressListener != null) {
                  progressListener.onADialogsProgressCancel(dialog);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      if (listener != null)
         pd.setButton(DialogInterface.BUTTON_POSITIVE, "Interrupt", listener);
   }

   public void showProgress() {
      pd.show();
   }

   public void cancelProgress() {
      pd.dismiss();
   }

   public void setADialogsImageAlertListener(ADialogsImageAlertListener imageAlertListener) {
      this.imageAlertListener = imageAlertListener;
   }

   public void customErrorDialog(String message, DialogInterface.OnClickListener listener) {
      AlertDialog.Builder ad = build(false, context.getString(R.string.error), message);
      ad.setPositiveButton("Ok", listener);
      ad.create().show();
   }

   public void customImageDialog(Context context, boolean cancelable, String title, File file, String positiveButton, String negativeButton) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View listLayout = inflater.inflate(R.layout.dialog_image, null);

      ImageView image = (ImageView) listLayout.findViewById(R.id.imageView);
      if (file.exists()) {
         Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
         image.setImageBitmap(bitmap);
      }

      AlertDialog.Builder ad = build(cancelable, title, null);
      ad.setView(listLayout);
      if (positiveButton != null) {
         ad.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               if (imageAlertListener != null) {
                  imageAlertListener.onADialogsImageAlertPositiveClick(dialog, id);
               } else {
                  dialog.cancel();
               }
            }
         });
      }
      if (negativeButton != null) {
         ad.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               dialog.cancel();
            }
         });
      }
      if (cancelable) {
         ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
               dialog.cancel();
            }
         });
      }
      ad.create().show();
   }

   //AlertDialog
   public interface ADialogsListener {
      public void onADialogsPositiveClick(DialogInterface dialog);

      public void onADialogsNegativeClick(DialogInterface dialog);

      public void onADialogsCancel(DialogInterface dialog);
   }

   // ListDialog
   public interface ADialogsListListener {
      public void onADialogsListDialog(DialogInterface dialog, int id, VkConstants.SharePost type);
   }

   //EditDialog
   public interface ADialogsEditListener {
      public void onADialogsPositiveClick(DialogInterface dialog, String text);

      public void onADialogsNegativeClick(DialogInterface dialog);

      public void onADialogsCancel(DialogInterface dialog);
   }

   public interface ADialogsProgressListener {
      public void onADialogsProgressCancel(DialogInterface dialog);
   }

   public interface ADialogsImageAlertListener {
      public void onADialogsImageAlertPositiveClick(DialogInterface dialog, int id);
   }
}
