package com.example.gzano.uniboors.Presenter

import android.util.Log
import android.view.View
import com.example.gzano.uniboors.Model.Room
import com.example.gzano.uniboors.Model.Room.*
import com.example.gzano.uniboors.Model.RoomType
import com.example.gzano.uniboors.ViewInterfaces.FragmentView.PlacesFragmentView
import com.example.gzano.uniboors.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by gzano on 04/10/2017.
 */
class PlacesPresenter(val placesFragmentView: PlacesFragmentView, private val databaseRef: DatabaseReference) : Presenter {

    private var images = HashMap<RoomType, File>()
    override fun onCreate() {


        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot?) {
                val fetchedRooms = ArrayList<Room>()
                if (p0 != null && p0.value == "empty") {
                    placesFragmentView.suggestUserToLookForPlaces()
                } else {
                    placesFragmentView.showProgressBar(placesFragmentView.getPageTag())

//                    }


                    p0?.children?.mapTo(fetchedRooms) {
                        when (it.value.toString()) {
                            Constants.CLASSROOM_NODE_VALUE -> {

                                ClassRoom(RoomType.CLASSROOM, it.key.toString(), false)

                            }
                            Constants.COMPUTER_LAB_NODE_VALUE -> {
                                ComputerLab(RoomType.COMPUTER_LAB, it.key.toString(), false)
                            }
                            else -> {
                                GenericRoom(RoomType.GENERIC, it.key.toString(), false)
                            }
                        }
                    }

                    val storage1 = FirebaseStorage.getInstance().reference.child("complab.jpg")

                    var localFile1: File?
                    try {
                        localFile1 = File.createTempFile("image1", "jpg")
                        storage1.getFile(localFile1).addOnSuccessListener {
                            images.put(RoomType.COMPUTER_LAB, localFile1!!)
                            val storage2 = FirebaseStorage.getInstance().reference.child("job-day5.jpg")
                            var localFile2: File?
                            try {
                                localFile2 = File.createTempFile("image2", "jpg")
                                storage2.getFile(localFile2).addOnSuccessListener {
                                    images.put(RoomType.CLASSROOM, localFile2!!)
                                    placesFragmentView.setAdapter(fetchedRooms, images)
                                    placesFragmentView.hideProgressBar(placesFragmentView.getPageTag())
                                    Log.d("TESTFILE ", " map " + images)
                                }

                            } catch (e: IOException) {
                                e.printStackTrace()
                            }


                        }


                    } catch (e: IOException) {
                        e.printStackTrace()
                    }


                }

            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            }

        })


    }

    fun addToFavPlaces(room: Room) {


        val databaseRef = FirebaseDatabase.getInstance().getReference(Constants.NODE_USERS_PATH).
                child(FirebaseAuth.getInstance().currentUser?.uid).child("places")
        when (room.roomType) {
            RoomType.CLASSROOM -> checkIfPresent(databaseRef, room.roomName, Constants.CLASSROOM_NODE_VALUE)


            RoomType.COMPUTER_LAB -> checkIfPresent(databaseRef, room.roomName, Constants.COMPUTER_LAB_NODE_VALUE)
            else -> {
            }
        }
    }

    private fun checkIfPresent(databaseRef: DatabaseReference, roomName: String, value: String) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                if (p0 != null) {
                    //  placesFragmentView.showAlertGoToNavigationOrStay()
                    val isPresent = p0.children.any { it.key == roomName }
                    if (!isPresent && placesFragmentView.getPageTag() == Constants.PAGE_TAG_CESENA_PLACE) {
                        placesFragmentView.showAlertGoToNavigationOrStay(databaseRef.child(roomName), value)


                    }
                    if (isPresent && placesFragmentView.getPageTag() == Constants.PAGE_TAG_CESENA_PLACE) {
                        placesFragmentView.showGoAlert()
                    }
                    if (isPresent && placesFragmentView.getPageTag() == Constants.PAGE_TAG_YOUR_PLACE) {
                        placesFragmentView.startActivity()
                    }
                }
            }

        })
    }

    fun onLongPressed(view: View) {

        if (placesFragmentView.getPageTag() == Constants.PAGE_TAG_YOUR_PLACE) {
            placesFragmentView.showPopUp(view)
        }

    }

    fun removeRoom(roomName: String) {

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                p0?.children?.filter { it.key == roomName }?.forEach { it.ref.removeValue() }
            }
        })


    }
}


