package com.example.nearmekotlindemo.repo

import android.net.Uri
import com.example.nearmekotlindemo.UserModel
import com.example.nearmekotlindemo.constant.AppConstant
import com.example.nearmekotlindemo.utility.State
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class AppRepo {

    fun login(
        email: String,
        password: String
    ): kotlinx.coroutines.flow.Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val data = auth.signInWithEmailAndPassword(email, password).await()
        data?.let {
            if (auth.currentUser?.isEmailVerified!!) {
                emit(State.success("Login Successfully"))
            } else {
                auth.currentUser?.sendEmailVerification()?.await()
                emit(State.failed("Verify email first"))
            }
        }
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(
        Dispatchers.IO
    )

    fun signUp(
        email: String,
        password: String,
        username: String,
        image: Uri
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val data = auth.createUserWithEmailAndPassword(email, password).await()
        data.user?.let {
            val path = uploadImage(it.uid, image).toString()
            val userModel = UserModel(
                email, username, path
            )

            createUser(userModel, auth)

            emit(State.success("Email verification sent"))

        }

    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

    private suspend fun uploadImage(uid: String, image: Uri): Uri {
        val firebaseStorage = Firebase.storage
        val storageReference = firebaseStorage.reference
        val task = storageReference.child(uid + AppConstant.PROFILE_PATH)
            .putFile(image).await()

        return task.storage.downloadUrl.await()

    }

    private suspend fun createUser(userModel: UserModel, auth: FirebaseAuth) {
        val firebase = Firebase.database.getReference("Users")
        firebase.child(auth.uid!!).setValue(userModel).await()
        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userModel.username)
            .setPhotoUri(Uri.parse(userModel.image))
            .build()
        auth.currentUser?.apply {
            updateProfile(profileChangeRequest).await()
            sendEmailVerification().await()
        }
    }

    fun forgetPassword(email: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        auth.sendPasswordResetEmail(email).await()
        emit(State.success("Password reset email sent."))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)

}