package com.app.spark.activity.personal_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.ImportantDataModel
import com.app.spark.models.ImportantDataResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File
import java.util.*

class PersonalInfoViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<String>()
    private var userId: String? = null
    private var header: String? = null

    fun setUserDetail(header: String, userId: String) {
        this.header = header
        this.userId = userId
    }

    fun updatePersonalInfoAPI(
        nickname: String,
        about: String,
        relationship: String,
        birthdate: String,
        hobbies: List<String>,
        interest: List<String>,
        tags: List<String>,
        questionList: List<String>,
        answerList: List<String>
    ) {
        if (validateData(nickname, about, relationship, birthdate, hobbies, interest, tags)) {
            try {
                var hobby = ""
                for (item in hobbies) {
                    hobby = if (hobby.isEmpty())
                        item
                    else "$hobby,$item"
                }
                var intrst = ""
                for (item in interest) {
                    intrst = if (intrst.isEmpty())
                        item
                    else "$intrst,$item"
                }
                var tag = ""
                for (item in tags) {
                    tag = if (tag.isEmpty())
                        item
                    else "$tag,$item"
                }
                val quesArray = JSONArray(questionList)
                val ansArray = JSONArray(answerList)
                val map = hashMapOf<String, Any?>(
                    "user_id" to userId,
                    "nickname" to nickname,
                    "about" to about,
                    "relationship" to relationship,
                    "birthdate" to birthdate,
                    "hobbies" to hobby,
                    "interest" to intrst,
                    "tags" to tag,
                    "ques" to quesArray,
                    "answer" to ansArray,
                )

                Coroutines.main {
                    val result = ApiInterface(getApplication()).updatePersonalInfoApi(
                        header, map
                    )
                    if (result!!.isSuccessful) {
                        if (result.body()?.statusCode == 200) {
                            response.postValue(result.body()?.APICODERESULT)
                        } else {
                            errString.postValue(result.body()?.APICODERESULT)
                        }
                    } else {
                        errRes.postValue(R.string.something_went_wrong)
                    }
                }
            } catch (ex: Exception) {
                errRes.postValue(R.string.something_went_wrong)
            }
        }
    }

    private fun validateData(
        nickname: String, about: String, relationship: String,
        birthdate: String,
        hobbies: List<String>,
        interest: List<String>,
        tags: List<String>
    ): Boolean {
        return when {
            nickname.isEmpty() -> {
                errRes.value = R.string.error_nickname
                false
            }
            about.isEmpty() -> {
                errRes.value = R.string.error_personal_about
                false
            }
            birthdate.isEmpty() -> {
                errRes.value = R.string.error_birthdate
                false
            }
            relationship.isEmpty() -> {
                errRes.value = R.string.error_relationship
                false
            }
            hobbies.isEmpty() -> {
                errRes.value = R.string.error_hobbies
                false
            }
            interest.isEmpty() -> {
                errRes.value = R.string.error_interest
                false
            }
            tags.isEmpty() -> {
                errRes.value = R.string.error_tags
                false
            }

            else -> true
        }
    }
}