package com.example.yourway.fetchpost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.example.yourway.R
class PostFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var commentButton: Button
    private lateinit var commentCountTextView: TextView
    private lateinit var post: Post

    companion object {
        private const val ARG_POST_ID = "postId"
        fun newInstance(postId: String): PostFragment {
            val fragment = PostFragment()
            val args = Bundle()
            args.putString(ARG_POST_ID, postId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        viewPager = view.findViewById(R.id.viewPagerImages)
        commentButton = view.findViewById(R.id.commentButton)
        commentCountTextView = view.findViewById(R.id.commentCountTextView)

        val postId = arguments?.getString(ARG_POST_ID) ?: return null
        fetchPostDetails(postId)

        commentButton.setOnClickListener {
            openCommentBottomSheet(postId)
        }

        return view
    }

    private fun fetchPostDetails(postId: String) {
//        // Fetch post details by postId from backend (or Firestore)
//        post = /* get post from DB */
//
//        // Set up ViewPager for images
//        val adapter = ImagePagerAdapter(post.imageUrls)
//        viewPager.adapter = adapter
//
//        // Set comment count
//        commentCountTextView.text = "${post.commentCount} Comments"
    }

    private fun openCommentBottomSheet(postId: String) {
        val bottomSheetFragment = CommentBottomSheetFragment.newInstance(postId)
        bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
    }
}
