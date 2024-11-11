package org.jaqpot.exception

import okhttp3.ResponseBody

class JaqpotSDKException(message: String, val errorBody: ResponseBody? = null) : Exception(message)
