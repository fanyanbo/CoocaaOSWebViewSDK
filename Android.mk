# 将此工程作为一个Libs的集成方法

# WEBVIEWSDK_PATH要根据工程实际目录调整
# WEBVIEWSDK_PATH := ../../../Framework/SkyAndroidLibrary/SystemWebViewSDK
# LOCAL_SRC_FILES := $(call all-java-files-under, src $(WEBVIEWSDK_PATH)/src)
# LOCAL_RESOURCE_DIR += $(addprefix $(LOCAL_PATH)/, res $(WEBVIEWSDK_PATH)/res)
# LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.coocaa.systemwebview

# 另外还需要依赖 SkyJavaLibrary/SystemWebViewSDKExtra 的库
#  LOCAL_STATIC_JAVA_LIBRARIES += SystemWebViewSDKExtra

# 并去掉之前的
# LOCAL_SRC_FILES := $(call all-java-files-under, src)