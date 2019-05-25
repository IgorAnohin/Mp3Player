package anokhin.underground.mp3player

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog.*
import java.io.File

class MainActivity : AppCompatActivity() {

    val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0
    val CUSTOM_DIALOG_ID = 0
    lateinit var root: File
    lateinit var curFolder: File
    lateinit var backToParent: Button
    lateinit var pickFolder: Button
    lateinit var dialogFilesList: ListView
    lateinit var currentFolderText: TextView
    val fileList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = File(Environment.getExternalStorageDirectory().absolutePath)
        curFolder = root

        setContentView(R.layout.activity_main)
        search_button?.setOnClickListener{

            Log.i("Own", "Here")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

                Log.i("Own", "lol")
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                }

                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

            } else {
                showFolders()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    showFolders()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun showFolders() {
        showDialog(CUSTOM_DIALOG_ID)
    }

    override fun onCreateDialog(id: Int): Dialog? {
        var dialog: Dialog? = null

        when (id) {
            CUSTOM_DIALOG_ID -> {
                dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog)
                dialog.setTitle("File Explorer")
                dialog.setCancelable(true)
                dialog.setCanceledOnTouchOutside(true)

                backToParent = dialog.findViewById(R.id.back_to_parent)
                pickFolder = dialog.findViewById(R.id.pick_folder)
                dialogFilesList = dialog.findViewById(R.id.dialog_files_list)
                currentFolderText = dialog.findViewById(R.id.current_folder)

                backToParent.setOnClickListener {
                    listDir(curFolder.parentFile)
                }

                dialogFilesList.setOnItemClickListener { parent, view, position, id ->
                    val selected = File(fileList[position])
                    if (selected.isDirectory) {
                        listDir(selected)
                    } else {
                        val toast = Toast.makeText(applicationContext, "Only folders may be picked", Toast.LENGTH_LONG)
                        toast.show()
                    }
                }
            }
            else -> {

            }
        }

        return dialog
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog?) {
        super.onPrepareDialog(id, dialog)
        when(id) {
            CUSTOM_DIALOG_ID -> {
                listDir(curFolder)
            }
            else -> {
                // nope
            }
        }
    }

    private fun listDir(file: File) {
        backToParent.isEnabled = !file.equals(root)
        curFolder = file
        currentFolderText.text = curFolder.path

        val files = file.listFiles()
        fileList.clear()

        var haveMp3FileInside = false
        for (f in files) {
            if (f.name.endsWith(".mp3"))
                haveMp3FileInside = true
            fileList.add(f.path)
        }
        pickFolder.isEnabled = haveMp3FileInside

        val directoryListAdapter = ArrayAdapter<String>(this,
            R.layout.simple_list_item_1, fileList)
        dialogFilesList.adapter = directoryListAdapter
    }
}
