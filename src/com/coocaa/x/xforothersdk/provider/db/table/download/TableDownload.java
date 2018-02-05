package com.coocaa.x.xforothersdk.provider.db.table.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.coocaa.x.xforothersdk.app.SuperXFinder;
import com.coocaa.x.xforothersdk.app.SuperXFinder.XIntent;
import com.coocaa.x.xforothersdk.framework.pm.XPackageManager;
import com.coocaa.x.xforothersdk.framework.utils.MD5;
import com.coocaa.x.xforothersdk.provider.ProviderData;
import com.coocaa.x.xforothersdk.provider.db.Table;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by lu on 15-12-23.
 */
public class TableDownload extends Table {
    public static final String URI_PATH = "default/download";

    public enum COLUMNS {
        NAME("name"), USERID("userid"), URL("url"), SAVEDIR("savedir"), SAVENAME("savename"),
        MD5("md5"), CURRENT("current"), LENGTH("length"), STATUS("status"), EXTRA("extra"), STARTTIME("starttime"),
        CREATETIME("createtime"), ONCODE("oncode"), ONEXTRA("onextra"), SPEED("speed"), FROM("_from"), COMPLETEACTION("completeaction");

        public String name;

        COLUMNS(String name) {
            this.name = name;
        }
    }

    public static final String WITHOUT_MD5 = "WITHOUT_MD5";

    private String name;
    private String userid, url, savedir, savename;
    private String md5 = WITHOUT_MD5;
    private long current, length;
    private int status;
    private long starttime, createtime = System.currentTimeMillis();
    private String extra;
    private float speed;

    private int oncode;
    private String onextra;

    private String completeaction;

    private String _from;

    public String getFrom() {
        return _from;
    }

    public void setFrom(String from) {
        this._from = from;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOncode() {
        return oncode;
    }

    public void setOncode(int oncode) {
        this.oncode = oncode;
    }

    public String getOnextra() {
        return onextra;
    }

    public void setOnextra(String onextra) {
        this.onextra = onextra;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSavedir() {
        return savedir;
    }

    public void setSavedir(String savedir) {
        this.savedir = savedir;
    }

    public String getSavename() {
        return savename;
    }

    public void setSavename(String savename) {
        this.savename = savename;
    }

    public XIntent getCompleteAction() {
        try {
            return XIntent.parseJObject(completeaction, XIntent.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setCompleteAction(XIntent xintent) {
        this.completeaction = xintent.toJSONString();
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public DOWNLOAD_STATUS getStatus() {
        return DOWNLOAD_STATUS.values()[status];
    }

    public void setStatus(DOWNLOAD_STATUS status) {
        this.status = status.ordinal();
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getCreatetime() {
        return createtime;
    }

    public String getSavedFilePath() {
        return savedir + File.separator + savename;
    }

    public int getProgress() {
        return (int) (100 * getCurrent() / getLength());
    }

    public static final String DOWNLOAD_EXTRA_ICON_URL = "DOWNLOAD_EXTRA_ICON_URL";

    public synchronized void putExtra(String key, String value) {
        Map<String, String> _extra = null;
        try {
            _extra = (Map<String, String>) JSONObject.parse(extra);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_extra == null)
            _extra = new HashMap<String, String>();
        _extra.put(key, value);
        extra = JSONObject.toJSONString(_extra);
    }

    public synchronized String getExtra(String key) {
        Map<String, String> _extra = null;
        try {
            _extra = (Map<String, String>) JSONObject.parse(extra);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (_extra == null)
            return null;
        return _extra.get(key);
    }


    public enum ExtraDownloadApp {
        APPID, PACKAGENAME, ICONURL
    }

    private static final String TDL = "TDL";
    public static final String DOWNLOAD_STATUS_RECEIVER_ACTION = "com.coocaa.x.DOWNLOAD_STATUS_RECEIVER_ACTION";

    private static Map<TableDownloadListener, BroadcastReceiver> tableDownloadListeners = new HashMap<TableDownloadListener, BroadcastReceiver>();

    /**
     * 创建下载监听器
     *
     * @param c
     * @param l {@link com.coocaa.x.provider.db.tables.download.TableDownload.TableDownloadListener}
     */
    public static void _createTableDownloadListener(Context c, final TableDownloadListener l) {
        synchronized (tableDownloadListeners) {
            if (tableDownloadListeners.containsKey(l))
                return;
            BroadcastReceiver r = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        if (l != null) {
                            long id = intent.getLongExtra(COLUMNS_ID, -1);
                            if (id != -1) {
                                int status = intent.getIntExtra(COLUMNS.STATUS.name, -1);
                                int code = intent.getIntExtra(COLUMNS.ONCODE.name, 0);
                                String extra = intent.getStringExtra(COLUMNS.ONEXTRA.name);
                                Log.d(TDL, "onReceive id:" + id + " status:" + status + " code:" + code + " extra:" + extra);
                                switch (DOWNLOAD_STATUS.values()[status]) {
                                    case ON_DEFAULT: {
                                        l.onEnqueued(id);
                                        break;
                                    }
                                    case ON_STARTING: {
                                        l.onStarting(id, code, extra);
                                        break;
                                    }
                                    case ON_DOWNLOADING: {
                                        l.onStartDownloading(id, code, extra);
                                        break;
                                    }
                                    case ON_PAUSED: {
                                        l.onPaused(id, code, extra);
                                        break;
                                    }
                                    case ON_STOPPED: {
                                        l.onStopped(id, code, extra);
                                        break;
                                    }
                                    case ON_COMPLETE: {
                                        l.onComplete(id, code, extra);
                                        break;
                                    }
                                    case ON_REMOVED: {
                                        l.onRemoved(id, code, extra);
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(DOWNLOAD_STATUS_RECEIVER_ACTION);
            c.registerReceiver(r, filter);
            tableDownloadListeners.put(l, r);
        }
    }

    /**
     * 释放下载监听器
     *
     * @param c
     * @param l 需要释放的 {@link com.coocaa.x.provider.db.tables.download.TableDownload.TableDownloadListener}
     */
    public static void _destroyTableDownloadListener(Context c, TableDownloadListener l) {
        synchronized (tableDownloadListeners) {
            if (tableDownloadListeners.containsKey(l)) {
                BroadcastReceiver r = tableDownloadListeners.get(l);
                try {
                    if (r != null)
                        c.unregisterReceiver(r);
                } catch (Exception e) {
                }
                tableDownloadListeners.remove(l);
            }
        }
    }

    /**
     * 下载过程回调监听接口
     */
    public interface TableDownloadListener extends TableDownloadMonitor {
        /**
         * 调用 {@link #_enqueue(TableDownload)} 成功之后的回调，之后便可调用 {@link #_start(long)} {@link #_startNow(long)}
         *
         * @param id 任务id
         */
        void onEnqueued(long id);

        /**
         * 调用{@link #_start(long)}成功之后的回调，表明任务进入了 {@link #DOWNLOAD_STATUS}
         *
         * @param id    任务id
         * @param code  扩展code
         * @param extra 扩展extra
         */
        void onStarting(long id, int code, String extra);

        /**
         * 调用{@link #_start(long)} {@link #_startNow(long)}成功之后
         * 并进入{@link #DOWNLOAD_STATUS}的回调，每次进入状态后只会回调一次，如果要查询下载
         * 过程的进度等信息，{@link #_queryDownload(long)} {@link #_queryDownloadByUserID(String)}
         *
         * @param id    任务id
         * @param code  扩展code
         * @param extra 扩展extra
         */
        void onStartDownloading(long id, int code, String extra);

        /**
         * 调用 {@link #_pause(long)}成功之后的回调 任务被暂停
         *
         * @param id    任务id
         * @param code  扩展code
         * @param extra 扩展extra
         */
        void onPaused(long id, int code, String extra);

        /**
         * 任务完成后的回调
         *
         * @param id    任务id
         * @param code  扩展code
         * @param extra 扩展extra
         */
        void onComplete(long id, int code, String extra);

        /**
         * 调用 {@link #_remove(long)}成功之后的回调，任务被删除
         *
         * @param id    任务id
         * @param code  扩展code
         * @param extra 扩展extra
         */
        void onRemoved(long id, int code, String extra);

        /**
         * 下载任务进入stopped状态
         *
         * @param id    任务id
         * @param code  进入stopped状态的原因code
         * @param extra 入stopped状态的原因扩展信息
         */
        void onStopped(long id, int code, String extra);
    }

    /**
     * 调用 {@link #_enqueue(TableDownload)}失败返回，任务已存在（url和savedir、savename完全相同）
     */
    public static final int ENQUEUE_ERROR_ALREADYEXIST = -1001;

    /**
     * 调用 {@link #_enqueue(TableDownload)}失败返回，数据异常
     */
    public static final int ENQUEUE_ERROR_DATAERROR = -1002;

    public enum DOWNLOAD_STATUS {
        ON_DEFAULT,
        TO_START,
        TO_START_NOW,
        TO_REMOVE,
        TO_PAUSE,
        ON_DOWNLOADING,
        ON_PAUSED,
        ON_STOPPED,
        ON_COMPLETE,
        ON_REMOVED,
        ON_STARTING
    }
//
//    public static final int STATUS_ON_DEFAULT = DOWNLOAD_STATUS.ON_DEFAULT.ordinal();
//    public static final int STATUS_TO_START = DOWNLOAD_STATUS.TO_START.ordinal();
//    public static final int STATUS_TO_START_NOW = DOWNLOAD_STATUS.TO_START_NOW.ordinal();
//    public static final int STATUS_TO_REMOVE = DOWNLOAD_STATUS.TO_REMOVE.ordinal();
//    public static final int STATUS_TO_PAUSE = DOWNLOAD_STATUS.TO_PAUSE.ordinal();
//    public static final int STATUS_ON_DOWNLOADING = DOWNLOAD_STATUS.ON_DOWNLOADING.ordinal();
//    public static final int STATUS_ON_PAUSED = DOWNLOAD_STATUS.ON_PAUSED.ordinal();
//    public static final int STATUS_ON_STOPPED = DOWNLOAD_STATUS.ON_STOPPED.ordinal();
//    public static final int STATUS_ON_COMPLETE = DOWNLOAD_STATUS.ON_COMPLETE.ordinal();
//    public static final int STATUS_ON_REMOVED = DOWNLOAD_STATUS.ON_REMOVED.ordinal();
//    public static final int STATUS_ON_STARTING = DOWNLOAD_STATUS.ON_STARTING.ordinal();


    public static final int ONSTOP_CODE_ERROR_NO_DISK_SPACE = -1000;
    public static final int ONSTOP_CODE_ERROR_MD5_CHECK_ERROR = -1001;

    public static final Uri TABLEDOWNLOAD_URI = SuperXFinder.CC_APPSTORE_AUTH_FINDER.createDBUri(TableDownload.URI_PATH).toUri();

    private static String getNameFromUrl(String url) {
        return MD5.md5s(url);
    }


    /**
     * 按照url创建下载任务 {@link TableDownload}
     *
     * @param url 下载rul
     * @return {@link TableDownload}
     */
    public static TableDownload _newDownload(String url) {
        TableDownload t = new TableDownload();
        t.setStatus(DOWNLOAD_STATUS.ON_DEFAULT);
        t.setUrl(url);
        t.setSavename(getNameFromUrl(url) + ".apk");
        t.setName(t.getSavename());
        return t;
    }


    /**
     * 获取所有的下载任务列表
     *
     * @return 成功返回非空的任务表
     */
    public static List<TableDownload> _queryDownloadList() {
        Cursor c = CR.query(TABLEDOWNLOAD_URI, null, null, null, null);
        List<TableDownload> list = ProviderData.listFromCursor(c, TableDownload.class);
        c.close();
        return list;
    }

    public static List<TableDownload> _queryDownloadListByStatus(DOWNLOAD_STATUS stauts) {
        Cursor c = CR.query(TABLEDOWNLOAD_URI, null, COLUMNS.STATUS.name + "=?", new String[]{String.valueOf(stauts)}, null);
        List<TableDownload> list = ProviderData.listFromCursor(c, TableDownload.class);
        c.close();
        return list;
    }

    /**
     * 通过任务的{@link TableDownload#id}来查询任务
     *
     * @param id 指定任务的 {@link TableDownload#id}
     * @return 成功返回非空 {@link TableDownload}
     */
    public static TableDownload _queryDownload(long id) {
        Cursor c = CR.query(TABLEDOWNLOAD_URI, null, Table.COLUMNS_ID + "=?", new String[]{String.valueOf(id)}, null);
        TableDownload t = ProviderData.objectFromCursor(c, TableDownload.class);
        c.close();
        return t;
    }

    /**
     * 通过任务的{@link TableDownload#url}来查询任务
     *
     * @param url 指定任务的 {@link TableDownload#url}
     * @return 成功返回非空 {@link TableDownload}
     */
    public static TableDownload _queryDownloadByUrl(String url) {
        Cursor c = CR.query(TABLEDOWNLOAD_URI, null, COLUMNS.URL + "=?", new String[]{url}, null);
        TableDownload t = ProviderData.objectFromCursor(c, TableDownload.class);
        c.close();
        return t;
    }

    /**
     * 通过任务的{@link TableDownload#userid}来查询任务
     *
     * @param userid 指定任务的 {@link TableDownload#userid}
     * @return 成功返回非空 {@link TableDownload}
     */
    public static TableDownload _queryDownloadByUserID(String userid) {
        Cursor c = CR.query(TABLEDOWNLOAD_URI, null, COLUMNS.USERID.name + "=?", new String[]{userid}, null);
        TableDownload t = ProviderData.objectFromCursor(c, TableDownload.class);
        c.close();
        return t;
    }

    /**
     * 将下载任务插入队列，但没有进入下载器可管理的状态，需要调用{@link #_startNow}或{@link #_start}
     *
     * @param t 入队的下载任务
     * @return 成功返回大于0的任务id，否则返回小于0的错误码 {@link #ENQUEUE_ERROR_ALREADYEXIST} {@link #ENQUEUE_ERROR_DATAERROR}
     */
    public static int _enqueue(TableDownload t) {
        Uri uri = CR.insert(TABLEDOWNLOAD_URI, t.toContentValues());
        return Integer.valueOf(uri.getQueryParameter("id"));
    }

    /**
     * 优先指定任务id的任务
     *
     * @param id 指定任务id
     * @return
     */
    public static boolean _startNow(long id) {
        TableDownload t = _queryDownload(id);
        if (t == null)
            return false;
        t.setStatus(DOWNLOAD_STATUS.TO_START_NOW);
        return CR.update(TABLEDOWNLOAD_URI, t.toContentValues(), Table.COLUMNS_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }

    /**
     * 开始指定任务id 的任务
     *
     * @param id 指定任务id
     * @return
     */
    public static boolean _start(long id) {
        TableDownload t = _queryDownload(id);
        if (t == null)
            return false;
        t.setStatus(DOWNLOAD_STATUS.TO_START);
        return CR.update(TABLEDOWNLOAD_URI, t.toContentValues(), Table.COLUMNS_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }

    public static void _startAll() {
        List<TableDownload> list = _queryDownloadList();
        for (TableDownload download : list)
            _start(download.getId());
    }

    /**
     * 暂停指定任务id 的任务
     *
     * @param id 指定任务id
     * @return
     */
    public static boolean _pause(long id) {
        TableDownload t = _queryDownload(id);
        if (t == null)
            return false;
        t.setStatus(DOWNLOAD_STATUS.TO_PAUSE);
        return CR.update(TABLEDOWNLOAD_URI, t.toContentValues(), Table.COLUMNS_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }

    public static void _pauseAll() {
        List<TableDownload> list = _queryDownloadList();
        for (TableDownload download : list)
            _pause(download.getId());
    }

    /**
     * 删除指定任务id 的任务
     *
     * @param id 指定任务id
     * @return
     */
    public static boolean _remove(long id) {
        TableDownload t = _queryDownload(id);
        if (t == null)
            return false;
        t.setStatus(DOWNLOAD_STATUS.TO_REMOVE);
        return CR.update(TABLEDOWNLOAD_URI, t.toContentValues(), Table.COLUMNS_ID + "=?", new String[]{String.valueOf(id)}) == 1;
    }

    public interface TableDownloadMonitor {
        void onDownloading(TableDownload t);
    }

    private static List<TableDownloadMonitor> monitors = new ArrayList<TableDownloadMonitor>();
    private static Timer monitorTimer = null;

    public static boolean _addTableDownloadMonitor(TableDownloadMonitor monitor) {
        synchronized (monitors) {
            if (monitors.size() == 0) {
                if (monitorTimer != null) {
                    monitorTimer.cancel();
                    monitorTimer = null;
                }
                monitorTimer = new Timer();
                monitorTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        List<TableDownload> list = _queryDownloadList();
                        if (list == null || list.size() == 0)
                            return;
                        for (TableDownload download : list) {
                            if (download.getStatus() == DOWNLOAD_STATUS.ON_DOWNLOADING) {
                                synchronized (monitors) {
                                    for (TableDownloadMonitor m : monitors) {
                                        try {
                                            m.onDownloading(download);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }, 0, 1000);
            }
            if (monitors.contains(monitor))
                return false;
            monitors.add(monitor);
            return true;
        }
    }

    public static void _removeTableDownloadMonitor(TableDownloadMonitor monitor) {
        synchronized (monitors) {
            monitors.remove(monitor);
            if (monitors.size() == 0) {
                if (monitorTimer != null) {
                    monitorTimer.cancel();
                    monitorTimer = null;
                }
            }
        }
    }


    /**
     * 创建应用下载任务
     *
     * @param url         下载地址
     * @param md5         md5校验值，如果不存在则传null
     * @param title       下载的任务名字
     * @param packageName 应用包名
     * @param appID       应用的appID
     * @param iconUrl     应用iconURL
     * @return 失败返回null，成功则返回TableDownload对象，接着调用{@link TableDownload#_start(long)}方法启动任务
     */
    public static TableDownload _createAppDownload(String url, String md5, String title, String packageName, String appID, String iconUrl) {
        TableDownload download = TableDownload._queryDownloadByUserID(packageName);
        if (download == null) {
            download = TableDownload._newDownload(url);
            download.setName(title);
            download.setUserid(packageName);
            download.putExtra(TableDownload.ExtraDownloadApp.APPID.toString(), appID);
            download.putExtra(TableDownload.ExtraDownloadApp.PACKAGENAME.toString(), packageName);
            download.putExtra(TableDownload.ExtraDownloadApp.ICONURL.toString(), iconUrl);
            if (!TextUtils.isEmpty(md5))
                download.setMd5(md5);

            XIntent action = null;
            try {
                XPackageManager.InstallExtraBuilder extrabuilder = XPackageManager.InstallExtraBuilder.builder().setDeleteArchiveOnSuccess(true);
                action = SuperXFinder.InstallPackageCustomActionBuilder.createBuilder().setInstallExtra(extrabuilder).build();
            } catch (SuperXFinder.CustomActionBuilder.CustomActionBuilderException e) {
                e.printStackTrace();
            }

            download.setCompleteAction(action);
            long id = TableDownload._enqueue(download);
            if (id > 0) {
                return TableDownload._queryDownload(id);
            }
            return null;
        }
        return download;
    }
}
