 package com.lamhong.mybook.Fragment

import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lamhong.mybook.Adapter.PostAdapter
import com.lamhong.mybook.Adapter.UserAdapter
import com.lamhong.mybook.DetailPostFragment
import com.lamhong.mybook.Models.Post
import com.lamhong.mybook.Models.SharePost
import com.lamhong.mybook.Models.TimelineContent
import com.lamhong.mybook.Models.User
import com.lamhong.mybook.R
import kotlinx.android.synthetic.main.fragment_notify.*
import kotlinx.android.synthetic.main.fragment_z_home.*
import kotlinx.android.synthetic.main.fragment_z_home.view.*

 // TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [zHome.newInstance] factory method to
 * create an instance of this fragment.
 */
class zHome : Fragment() {

    lateinit var currentUser : FirebaseUser

    private var postAdapter : PostAdapter?=null
    private var followingList : MutableList<Post>?=null
    private var postList : MutableList<Post>?=null

    private var shareList: MutableList<SharePost> = ArrayList()
    private var lstTypeAdapter : List<Int> = ArrayList()
    private var lstIndex : List<Int> = ArrayList()

    private var postListID : List<String> = ArrayList()
    private var shareListID : List<String> = ArrayList()



    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_z_home, container, false)
        currentUser=FirebaseAuth.getInstance().currentUser



        checkFollowing()
        //(postList as ArrayList).add(getPost("-Mbg2gLimdhyWjSh0Tc7"))
        retrievePosts1()

        postList= ArrayList()


        var recycleView: RecyclerView ?=null
        recycleView= view.findViewById(R.id.recycleviewzHome)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true
        recycleView.layoutManager= linearLayoutManager
        postAdapter= context?.let { PostAdapter(it, postList as ArrayList<Post> , lstIndex as ArrayList,
            lstTypeAdapter as ArrayList, shareList as ArrayList) }
        recycleView.adapter= postAdapter
        recycleView?.visibility= View.VISIBLE

        // search function
        view.searchbtn.setOnClickListener{
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, HomeFragment()).commit()
        }
        return view;
    }

        private fun checkFollowing() {
            followingList=ArrayList()

        val followRef = FirebaseDatabase.getInstance().reference
            .child("Friends").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("friendList")
        followRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    (followingList as ArrayList<String>).clear()
                    for (s in snapshot.children){
                        s.key?.let { (followingList as ArrayList<String>).add(it) }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    private fun getPostShared(id: String): SharePost{
        val shareRef= FirebaseDatabase.getInstance().reference.child("Share Posts").child(id)
        var sharePost: SharePost?=null
        shareRef.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                     sharePost = snapshot.getValue<SharePost>(SharePost::class.java)
                }
            }
        })
        return sharePost as SharePost
    }

    private fun retrievePosts1(){
        val postRef= FirebaseDatabase.getInstance().reference.child("Contents")
        postRef.addValueEventListener(object:  ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    shareList!!.clear()
                    postList!!.clear()
                    (lstIndex as ArrayList).clear()
                    (lstTypeAdapter as ArrayList).clear()
                    var ind1 = 0
                    var ind2 = 0
                    for (s in snapshot.child("UserTimeLine")
                        .child(currentUser.uid).children) {
                        val tl = s.getValue(TimelineContent::class.java)
                        tl!!.setPostType(s.child("post_type").value.toString())
                        if (tl!!.getPostType() == "sharepost") {

                            var sharePost = snapshot.child("Share Posts").child(tl.getId()).getValue<SharePost>(SharePost::class.java)
                                 shareList!!.add(sharePost!!)
                            (shareListID as ArrayList).add(tl.getId())
                            (lstTypeAdapter as ArrayList).add(1)
                            (lstIndex as ArrayList).add(ind1)
                            ind1 += 1
                        } else if (tl!!.getPostType() == "post") {


                            val post = snapshot.child("Posts").child(tl.getId()).getValue(Post::class.java)
                            postList!!.add(post!!)
                            (postListID as ArrayList).add(tl.getId())
                            (lstTypeAdapter as ArrayList).add(0)
                            (lstIndex as ArrayList).add(ind2)
                            ind2 += 1
                        }
                      //  getPostAndShare()

                        postAdapter!!.notifyDataSetChanged()
                    }
                }

            }
        })

    }

    private fun getPostAndShare(){
        for(p in postListID){
            postList!!.add(getPost(p))
        }
        for (s in shareListID){
            shareList!!.add(getPostShared(s))

        }


    }
    private fun getPost(id: String): Post{
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts").child(id)

        var post : Post?=null
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(it: DataSnapshot) {
                post = it.getValue(Post::class.java)
                post!!.setpost_image(it.child("post_image").value.toString())
                post!!.setpostContent(it.child("post_content").value.toString())
                post!!.setpost_id(it.child("post_id").value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
//        postRef.get().addOnSuccessListener {
//
//        }
        return post as Post

    }
    private fun retrievePosts() {
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object: ValueEventListener{


            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()
                for(ss in snapshot.children){
                    if(ss.exists())
                    {
                        val post = ss.getValue(Post::class.java)
                   //   val post = Post()
                        post!!.setpost_image(ss.child("post_image").value.toString())
                        post!!.setpostContent(ss.child("post_content").value.toString())
                        post.setpost_id(ss.child("post_id").value.toString())
                        for (idUser in (followingList as ArrayList<String>)) {
                        if(post!!.getpublisher() == idUser){
                        postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment zHome.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            zHome().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}