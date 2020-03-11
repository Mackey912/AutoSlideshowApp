package jp.techacademy.shohei.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    var count:Int=-1
    private var mTimer: Timer?=null
    private var mTimerSec=0.0
    private var mHandler=Handler()
    var checkp:Boolean=false//onRequestPermissionsResultで許可されたかの判別に使用

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for_button.setOnClickListener(this)
        back_button.setOnClickListener(this)

        sh_button.setOnClickListener {

            if (CheckPermission() == true) {

                if (mTimer == null) {
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mTimerSec += 0.1
                            mHandler.post {
                                count += 1
                                getContentsInfo(count)
                            }
                        }
                    }, 100, 2000)
                } else {
                    if (mTimer != null) {
                        mTimer!!.cancel()
                        mTimer = null
                    }
                }
            }
        }
    }

    override fun onClick(v:View){

        if (v.id == R.id.for_button){
            count+=1
        }else if(v.id==R.id.back_button){
            count-=1
        }
        if(CheckPermission()==true) getContentsInfo(count)
    }

    //permission処理の共通化
    private fun CheckPermission():Boolean{

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("ANDROID", "許可されている")
                return true
            } else {
                Log.d("ANDROID", "許可されていない")
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                if(checkp==false) return false
                else return true
            }
        }else{
            return true
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された")
                    checkp=true
                } else {
                    Log.d("ANDROID", "許可されなかった")
                }
        }
    }

    private fun getContentsInfo(count:Int){
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目(null = 全項目)
            null, // フィルタ条件(null = フィルタなし)
            null, // フィルタ用パラメータ
            null // ソート (null ソートなし)
        )

        if (cursor!!.moveToFirst()) {
            if(count>=0){
                for(i in 0..count-1)
                    if(cursor!!.moveToNext()){

                    }else{
                        cursor!!.moveToFirst()
                    }
            }else if(count<0){
                while(cursor!!.moveToNext()){}
            }

            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            img.setImageURI(imageUri)
        }
        cursor.close()
    }
}
