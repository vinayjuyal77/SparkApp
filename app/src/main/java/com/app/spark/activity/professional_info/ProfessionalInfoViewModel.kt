package com.app.spark.activity.professional_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import android.app.Application
import com.app.spark.R
import com.app.spark.models.AddEducationResponse
import com.app.spark.models.AddWorkExpResponse
import com.app.spark.models.ImportantDataModel
import com.app.spark.models.ImportantDataResponse
import com.app.spark.network.ApiInterface
import com.app.spark.utils.Coroutines
import com.app.spark.utils.getImageFilePart
import com.app.spark.utils.getRequestBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File
import java.time.Year
import java.util.*

class ProfessionalInfoViewModel(application: Application) : AndroidViewModel(application) {
    var errRes = MutableLiveData<Int>()
    var errString = MutableLiveData<String>()
    var response = MutableLiveData<String>()
    var workExpResponse = MutableLiveData<AddWorkExpResponse.Result>()
    var educationResponse = MutableLiveData<AddEducationResponse.Result>()
    private var userId: String? = null
    private var header: String? = null

    fun setUserDetail(header: String, userId: String) {
        this.header = header
        this.userId = userId
    }

    fun addWorkExperience(title: String, place: String, fromYear: String, toYear: String) {

        val map = hashMapOf<String, Any?>(
            "user_id" to userId,
            "place" to place,
            "work_position" to title,
            "from_year" to fromYear,
            "to_year" to toYear
        )
        try {
            Coroutines.main {
                val result = ApiInterface(getApplication()).addWorkExpApi(
                    header, map
                )
                if (result.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        workExpResponse.postValue(result.body()?.result)
                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }

    fun addEducation(
        education_title: String,
        collegeName: String,
        start_date: String,
        end_date: String
    ) {
        val map = hashMapOf<String, Any?>(
            "user_id" to userId,
            "college_name" to collegeName,
            "education_title" to education_title,
            "start_date" to start_date,
            "end_date" to end_date
        )
        try {
            Coroutines.main {
                val result = ApiInterface(getApplication()).addEducationApi(
                    header, map
                )
                if (result.isSuccessful) {
                    if (result.body()?.statusCode == 200) {
                        educationResponse.postValue(result.body()?.result)
                    } else {
                        errString.postValue(result.body()?.aPICODERESULT)
                    }
                } else {
                    errRes.postValue(R.string.something_went_wrong)
                }
            }
        } catch (ex: Exception) {
            errRes.postValue(R.string.something_went_wrong)
        }
    }

    fun updateProfessionalInfoAPI(
        title: String,
        about: String,
        contact: String,
        interest: List<String>,
        skills: List<String>,
        tags: List<String>,
        experience: List<AddWorkExpResponse.Result>,
        education: List<AddEducationResponse.Result>
    ) {
        if (validateData(title, about, contact, interest, skills, tags, experience, education)) {
            try {
                var skill = ""
                for (item in skills) {
                    skill = if (skill.isEmpty())
                        item
                    else "$skill,$item"
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
                var exp = ""
                for (item in experience) {
                    exp = if (tag.isEmpty())
                        item.expId
                    else "$exp,${item.expId}"
                }
                var edu = ""
                for (item in education) {
                    edu = if (tag.isEmpty())
                        item.educationId
                    else "$edu,${item.educationId}"
                }

                val map = hashMapOf<String, Any?>(
                    "user_id" to userId,
                    "title" to title,
                    "about" to about,
                    "country_code" to "91",
                    "contact" to contact,
                    "skills" to skill,
                    "interest" to intrst,
                    "tags" to tag,
                    "experience" to exp,
                    "education" to edu,
                )

                Coroutines.main {
                    val result = ApiInterface(getApplication()).updateProfessionalInfoApi(
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
        title: String,
        about: String,
        contact: String,
        interest: List<String>,
        skills: List<String>,
        tags: List<String>,
        experience: List<AddWorkExpResponse.Result>,
        education: List<AddEducationResponse.Result>
    ): Boolean {
        return when {
            title.isEmpty() -> {
                errRes.value = R.string.error_professional_title
                false
            }
            about.isEmpty() -> {
                errRes.value = R.string.error_personal_about
                false
            }
            contact.isEmpty() -> {
                errRes.value = R.string.error_professional_contact
                false
            }
            experience.isNullOrEmpty() -> {
                errRes.value = R.string.error_professional_exp
                false
            }
            skills.isEmpty() -> {
                errRes.value = R.string.error_professional_skill
                false
            }
            education.isEmpty() -> {
                errRes.value = R.string.error_professional_education
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