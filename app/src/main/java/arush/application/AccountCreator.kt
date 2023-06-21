import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

class AccountCreator(private val activity: Activity)  {

    private lateinit var phoneNumber: String

    interface ContactSelectionListener {
        fun onContactSelected(phoneNumber: String)
    }

    fun getContact(activity: Activity, listener: ContactSelectionListener) {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        activity.startActivityForResult(intent, CONTACT_REQUEST)
        contactSelectionListener = listener
    }

    companion object {
        private const val CONTACT_REQUEST = 1
        private var contactSelectionListener: ContactSelectionListener? = null
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
                val contentURI: Uri? = data?.data
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val cursor: Cursor? =
                    contentURI?.let { activity.contentResolver.query(it, projection, null, null, null) }
                cursor.use {
                    if (it != null && it.moveToFirst()) {
                        val numberIndex: Int = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        phoneNumber = it.getString(numberIndex)
                        contactSelectionListener?.onContactSelected(phoneNumber)
                    }
                }
            }
        } catch (e: Exception) {
            e.message?.let { Log.d("error", it) }
        }
    }
}
