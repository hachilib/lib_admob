package com.lib.hachi.appopenads

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class SweetAlertDialog(context: Context) : Dialog(context), View.OnClickListener {
    private val mDialogView: View? = null
    private val mModalInAnim: AnimationSet? = null
    private val mModalOutAnim: AnimationSet? = null
    private val mOverlayOutAnim: Animation? = null
    private val mErrorInAnim: Animation? = null
    private val mErrorXInAnim: AnimationSet? = null
    private val mSuccessLayoutAnimSet: AnimationSet? = null
    private val mSuccessBowAnim: Animation? = null
    private val mTitleTextView: TextView? = null
    private val mContentTextView: TextView? = null
    private val mCustomViewContainer: FrameLayout? = null
    private val mCustomView: View? = null
    private val mTitleText: String? = null
    private val mContentText: String? = null
    private val mShowCancel = false
    private val mShowContent = false
    private val mCancelText: String? = null
    private val mConfirmText: String? = null
    private val mNeutralText: String? = null
    private val mAlertType = 0
    private val mErrorFrame: FrameLayout? = null
    private val mSuccessFrame: FrameLayout? = null
    private val mProgressFrame: FrameLayout? = null
    //private val mSuccessTick: SuccessTickView? = null
    private val mErrorX: ImageView? = null
    private val mSuccessLeftMask: View? = null
    private val mSuccessRightMask: View? = null
    private val mCustomImgDrawable: Drawable? = null
    private val mCustomImage: ImageView? = null
    private val mButtonsContainer: LinearLayout? = null
    private val mConfirmButton: Button? = null
    private var mHideConfirmButton = false
    private val mCancelButton: Button? = null
    private val mNeutralButton: Button? = null
    private val mConfirmButtonBackgroundColor: Int? = null
    private val mConfirmButtonTextColor: Int? = null
    private val mNeutralButtonBackgroundColor: Int? = null
    private val mNeutralButtonTextColor: Int? = null
    private val mCancelButtonBackgroundColor: Int? = null
    private val mCancelButtonTextColor: Int? = null
    //private val mProgressHelper: ProgressHelper? = null
    private val mWarningFrame: FrameLayout? = null
    private val mCancelClickListener: OnSweetClickListener? = null
    private val mConfirmClickListener: OnSweetClickListener? = null
    private val mNeutralClickListener: OnSweetClickListener? = null
    private val mCloseFromCancel = false
    private val mHideKeyBoardOnDismiss = true
    private val contentTextSize = 0

    val NORMAL_TYPE = 0
    val ERROR_TYPE = 1
    val SUCCESS_TYPE = 2
    val WARNING_TYPE = 3
    val CUSTOM_IMAGE_TYPE = 4
    val PROGRESS_TYPE = 5


    var DARK_STYLE = false

    //aliases
    val BUTTON_CONFIRM = BUTTON_POSITIVE
    val BUTTON_CANCEL = BUTTON_NEGATIVE

    private val defStrokeWidth = 0f
    private val strokeWidth = 0f


    fun hideConfirmButton(): SweetAlertDialog? {
        mHideConfirmButton = true
        return this
    }

    interface OnSweetClickListener {
        fun onClick(sweetAlertDialog: SweetAlertDialog?)
    }

    override fun onClick(p0: View?) {

    }

}