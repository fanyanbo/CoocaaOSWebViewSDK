package org.apache.cordova.plugin.api.xforothersdk.framework.pm;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcelable;

import org.apache.cordova.plugin.api.xforothersdk.app.SuperXFinder;
import org.apache.cordova.plugin.api.xforothersdk.framework.data.JObject;

/**
 * Created by lu on 16-2-26.
 */
public class XPackageArchive extends JObject {
    public static abstract class XPackageArchiveBuilder {
        public final XPackageArchive build(String archiveUri) throws Exception {
            return build(archiveUri, null);
        }

        public abstract XPackageArchive build(String archiveUri, String packageName, String icon) throws Exception;

        public XPackageArchive build(String archiveUri, String icon) throws Exception {
            return build(archiveUri, null, icon);
        }
    }

    public static final XPackageArchiveBuilder ANDROID_BUILDER = new XPackageArchiveBuilder() {
        @Override
        public XPackageArchive build(String archiveUri, String packageName, String icon) throws Exception {
            PackageInfo packageInfo = SuperXFinder.getContext().getPackageManager().getPackageArchiveInfo(archiveUri,
                    PackageManager.GET_ACTIVITIES);
            if (packageInfo == null) {
                throw new Exception("INSTALL_FAILED_INVALID_APK");
            }

            XPackageArchive archive = new XPackageArchive();
            archive.packageType = XPackageManager.PACKAGE_TYPE_ANDROID;
            archive.archiveUri = archiveUri;
            archive.icon = icon;
            archive.addAttr(ATTR_PACKAGE_NAME, packageInfo.packageName);
            return archive;
        }
    };

    public static final class WebAppArchiveBuilder extends XPackageArchiveBuilder {
        public static final String EXTRA_LABEL = "extra.label";
        public static final String EXTRA_BROWSER = "extra.browser";

        private XPackageArchive archive = new XPackageArchive();

        private WebAppArchiveBuilder() {
            archive.packageType = XPackageManager.PACKAGE_TYPE_WEBAPP;
        }

        public WebAppArchiveBuilder newBuilder() {
            return new WebAppArchiveBuilder();
        }

        public WebAppArchiveBuilder setLabel(String label) {
            archive.addAttr(EXTRA_LABEL, label);
            return this;
        }

        public WebAppArchiveBuilder setBrowser(String browser) {
            archive.addAttr(EXTRA_BROWSER, browser);
            return this;
        }

        @Override
        public XPackageArchive build(String archiveUri, String pakcageName, String icon) throws Exception {
            archive.archiveUri = archiveUri;
            archive.icon = icon;
            archive.addAttr(ATTR_PACKAGE_NAME, pakcageName);
            return archive;
        }
    }

    public static final WebAppArchiveBuilder WEBAPP_BUILDER = new WebAppArchiveBuilder();

    public static final XPackageArchiveBuilder BUILDER = new XPackageArchiveBuilder() {

        @Override
        public XPackageArchive build(String archiveUri, String packageName, String icon) throws Exception {
            XPackageArchive archive = null;
            try {
                archive = ANDROID_BUILDER.build(archiveUri, packageName, icon);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (archive == null) {
                try {
                    archive = WEBAPP_BUILDER.build(archiveUri, packageName, icon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (archive == null)
                throw new Exception("");
            return archive;
        }
    };
    public static final String ATTR_PACKAGE_NAME = "";

    public int packageType;
    public String archiveUri;
    public String icon;

    public static final Parcelable.Creator<XPackageArchive> CREATOR = createCREATOR(XPackageArchive.class, null);
}
