package com.lamhong.mybook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.lamhong.mybook.Adapter.CommentAdapter
import com.lamhong.mybook.Models.Comment
import com.lamhong.mybook.Models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_setting.*
import kotlinx.android.synthetic.main.activity_comment.*

class  CommentActivity : AppCompatActivity() {

    private var postID : String= ""
    private var publisher: String = ""
    private var firebaseUser: FirebaseUser ?=null
    private var commentAdapter : CommentAdapter?=null
    private var commentList : MutableList<Comment>?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val intent = intent
        postID= intent.getStringExtra("postID").toString()
        publisher= intent.getStringExtra("publisher").toString()
        firebaseUser=FirebaseAuth.getInstance().currentUser
        userInfor()
        imageandOwnerInfor()
        getImage()
        btn_dangBinhLuan.setOnClickListener {
            if (TextUtils.isEmpty(edit_add_comment.text)) {
                Toast.makeText(this, "Nhập nội dung trước !!", Toast.LENGTH_LONG)
            } else {
                addComment()
            }
        }
        // add recycleview
        val recyclerView : RecyclerView
        recyclerView= findViewById(R.id.recycleview_comment)
        val linearLayoutManager : LinearLayoutManager= LinearLayoutManager(this)
        linearLayoutManager.reverseLayout=true
        recyclerView.layoutManager= linearLayoutManager


        commentList= ArrayList()
        commentAdapter= CommentAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter=commentAdapter
        recyclerView.visibility=View.VISIBLE

        viewComment()

    }
    private fun viewComment(){
        val commentRef= FirebaseDatabase.getInstance().reference
            .child("Comments").child(postID)
        commentRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    commentList!!.clear()
                    for(snap in snapshot.children){
                        val comment : Comment= snap.getValue(Comment::class.java)!!
                        comment.setOwner(snap.child("ownerComment").value.toString())
                        commentList!!.add(comment)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun addComment(){
        val commentRef= FirebaseDatabase.getInstance().reference
            .child("Comments").child(postID)
        val commentMap =HashMap<String, Any>()
        commentMap["content"]=edit_add_comment.text.toString()
        commentMap["ownerComment"]=firebaseUser!!.uid
        commentRef.push().setValue(commentMap)

        edit_add_comment.text.clear()
        addNotify()
    }
    private fun addNotify(){
        val notiRef= FirebaseDatabase.getInstance().reference.child("Notify")
            .child(publisher)
        val notiMap = HashMap<String,Any>()
        notiMap["userID"]=firebaseUser!!.uid
        notiMap["notify"]=edit_add_comment.text.toString()
        notiMap["postID"]=postID
        notiMap["type"]="binhluan"

        notiRef.push().setValue(notiMap)

    }
    private fun userInfor(){
        val userRef=FirebaseDatabase.getInstance().reference
            .child("UserInformation").child(firebaseUser!!.uid)
        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                   //Picasso.get().load(user!!.getAvatar()).into(image_post_incomment)
                   Picasso.get().load(user!!.getAvatar()).into(image_avatar_incomment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun imageandOwnerInfor(){
        val userref= FirebaseDatabase.getInstance().reference
            .child("UserInformation").child(publisher)
        userref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val fname= snapshot.child("fullname").value.toString()
                    tv_comment_appbar.text= "Bài viết của "+ fname
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
    private fun getImage(){
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts")
            .child(postID).child("post_image")

        postRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val imageContent= snapshot.value.toString()
                    Picasso.get().load(imageContent).into(image_post_incomment)
                }
                else {
                    Log.d("hong","nothing")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}