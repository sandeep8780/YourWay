package com.example.yourway.chat

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.chat.chatui.ChatInterfaceActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class ChatBaseFragment : Fragment() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var chatAdapter: ChatListAdapter
    private lateinit var fabCreateGroup: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var ivCancelSearch : ImageView

    private val chats = mutableListOf<Chat>()
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_base, container, false)

        chatRecyclerView = view.findViewById(R.id.rv_chats_userlist)
        searchEditText = view.findViewById(R.id.et_chats_search)
        fabCreateGroup = view.findViewById(R.id.fab_chat_newgroup)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_chats_base)
        ivCancelSearch = view.findViewById(R.id.iv_chat_cancel)

        // Setup RecyclerView
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
        chatAdapter = ChatListAdapter(chats, requireContext()) { chat ->
            startChatActivity(chat)
        }
        chatRecyclerView.adapter = chatAdapter

        // Setup Floating Action Button for creating groups
        fabCreateGroup.setOnClickListener {
            createGroupChat()
        }

        // Load chats initially
        loadChats()

        // Swipe to refresh logic
        swipeRefreshLayout.setOnRefreshListener {
            searchEditText.text.clear()

            ivCancelSearch.visibility = View.GONE
            loadChats()

        }

        // Setup search logic with debounce
        setupSearch()

        ivCancelSearch.setOnClickListener {
            searchEditText.text.clear()
            ivCancelSearch.visibility = View.GONE
            loadChats()
        }

        return view
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchRunnable?.let { handler.removeCallbacks(it) }  // Cancel previous search if it's still pending

                searchRunnable = Runnable {
                    s?.toString()?.let { query ->
                        performSearch(query)  // Perform search after debounce time
                    }
                }
                handler.postDelayed(searchRunnable!!, 2000)  // Wait for 2 seconds before triggering search
                if (ivCancelSearch.visibility == View.GONE){
                    ivCancelSearch.visibility= View.VISIBLE
                }
            }
        })
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) {
            // If search query is empty, reset to the original chat list
            chatAdapter.updateChats(chats)
            ivCancelSearch.visibility = View.GONE

            loadChats()
            return
        }

        val filteredChats = chats.filter { chat ->
            chat.groupName.contains(query, ignoreCase = true) || chat.displayName.contains(query, ignoreCase = true)
        }.sortedByDescending { it.lastMessageTime }  // Prioritize based on the last message timestamp

        chatAdapter.updateChats(filteredChats)
    }

    private fun createGroupChat() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Create New Group Chat")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_create_group_chat, null)
        builder.setView(dialogView)

        val etGroupName = dialogView.findViewById<EditText>(R.id.et_group_name)
        val etUsernames = dialogView.findViewById<EditText>(R.id.et_usernames)

        builder.setPositiveButton("Create") { _, _ ->
            val groupName = etGroupName.text.toString().trim()
            val usernamesInput = etUsernames.text.toString().trim()

            if (groupName.isEmpty() || usernamesInput.isEmpty()) {
                Toast("Please enter a group name and at least one username.", requireContext())
                return@setPositiveButton
            }

            val usernames = usernamesInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                .toMutableList()

            val currentUser = SharedPrefsHelper.getUsername(requireContext())
            currentUser?.let { usernames.add(it) }

            val chatData = hashMapOf(
                "groupName" to groupName,
                "participants" to usernames,
                "lastMessage" to "",
                "lastMessageTime" to System.currentTimeMillis(),
                "chatType" to "group"
            )

            FirebaseFirestore.getInstance().collection("chats")
                .add(chatData)
                .addOnSuccessListener {
                    Toast("Group chat created!", requireContext())
                    loadChats()
                }
                .addOnFailureListener { exception ->
                    Log.e("ChatBaseFragment", "Error creating group chat: ", exception)
                    Toast("Failed to create group chat.", requireContext())
                }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun loadChats() {
        val currentUser = SharedPrefsHelper.getUsername(requireContext()) ?: return

        FirebaseFirestore.getInstance().collection("chats")
            .whereArrayContains("participants", currentUser)
            .addSnapshotListener { snapshots, e ->
                swipeRefreshLayout.isRefreshing = false

                if (e != null || snapshots == null) {
                    Log.e("ChatBaseFragment", "Error loading chats: ", e)
                    Toast("Failed to load chats.", requireContext())
                    return@addSnapshotListener
                }

                chats.clear()
                for (document in snapshots.documents) {
                    val chat = document.toObject(Chat::class.java)

                    chat?.let {
                        chat.chatId = document.id
                        chats.add(it)
                    }
                }
                chatAdapter.notifyDataSetChanged()
            }
    }

    private fun startChatActivity(chat: Chat) {
//         Start chat activity with chat details
         val intent = Intent(context, ChatInterfaceActivity::class.java)
         intent.putExtra("chatId", chat.chatId)
         startActivity(intent)

    }
}
