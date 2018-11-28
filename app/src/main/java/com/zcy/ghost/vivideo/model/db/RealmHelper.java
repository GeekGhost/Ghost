package com.zcy.ghost.vivideo.model.db;

import com.zcy.ghost.vivideo.model.bean.Collection;
import com.zcy.ghost.vivideo.model.bean.Record;
import com.zcy.ghost.vivideo.model.bean.SearchKey;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Description: RealmHelper
 * Creator: yxc
 * date: 2016/9/21 17:46
 */

public class RealmHelper implements DBHelper {

    public static final String DB_NAME = "ghost.realm";
    private Realm mRealm;
    private static RealmHelper instance;


    public static RealmHelper getInstance() {
        if (instance == null) {
            synchronized (RealmHelper.class) {
                if (instance == null)
                    instance = new RealmHelper();
            }
        }
        return instance;
    }

    @Inject
    public RealmHelper() {
        mRealm = Realm.getInstance(new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name(DB_NAME)
                .build());
    }

    public Realm getRealm() {
        if (mRealm == null || mRealm.isClosed())
            mRealm = Realm.getDefaultInstance();
        return mRealm;
    }
    //--------------------------------------------------收藏相关----------------------------------------------------

    /**
     * 增加 收藏记录
     *
     * @param bean
     */
    public void insertCollection(Collection bean) {
        getRealm().beginTransaction();
        getRealm().copyToRealm(bean);
        getRealm().commitTransaction();
    }

    /**
     * 删除 收藏记录
     *
     * @param id
     */
    public void deleteCollection(String id) {
        Collection data = getRealm().where(Collection.class).equalTo("id", id).findFirst();
        getRealm().beginTransaction();
        data.deleteFromRealm();
        getRealm().commitTransaction();
    }

    /**
     * 清空收藏
     */
    public void deleteAllCollection() {
        getRealm().beginTransaction();
        getRealm().delete(Collection.class);
        getRealm().commitTransaction();
    }

    /**
     * 查询 收藏记录
     *
     * @param id
     * @return
     */
    public boolean queryCollectionId(String id) {
        RealmResults<Collection> results = getRealm().where(Collection.class).findAll();
        for (Collection item : results) {
            if (item.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 收藏列表
     *
     * @return
     */
    public List<Collection> getCollectionList() {
        //使用findAllSort ,先findAll再result.sort排序
        RealmResults<Collection> results = getRealm().where(Collection.class).findAll().sort("time", Sort.DESCENDING);
        return getRealm().copyFromRealm(results);
    }


    //--------------------------------------------------播放记录相关----------------------------------------------------

    /**
     * 增加播放记录
     *
     * @param bean
     * @param maxSize 保存最大数量
     */
    public void insertRecord(Record bean, int maxSize) {
        if (maxSize != 0) {
            RealmResults<Record> results = getRealm().where(Record.class).findAll().sort("time", Sort.DESCENDING);
            if (results.size() >= maxSize) {
                for (int i = maxSize - 1; i < results.size(); i++) {
                    deleteRecord(results.get(i).getId());
                }
            }
        }
        getRealm().beginTransaction();
        getRealm().copyToRealm(bean);
        getRealm().commitTransaction();
    }


    /**
     * 删除 播放记录
     *
     * @param id
     */
    public void deleteRecord(String id) {
        Record data = getRealm().where(Record.class).equalTo("id", id).findFirst();
        getRealm().beginTransaction();
        data.deleteFromRealm();
        getRealm().commitTransaction();
    }

    /**
     * 查询 播放记录
     *
     * @param id
     * @return
     */
    public boolean queryRecordId(String id) {
        RealmResults<Record> results = getRealm().where(Record.class).findAll();
        for (Record item : results) {
            if (item.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public List<Record> getRecordList() {
        //使用findAllSort ,先findAll再result.sort排序
        RealmResults<Record> results = getRealm().where(Record.class).findAll().sort("time", Sort.DESCENDING);
        return getRealm().copyFromRealm(results);
    }

    /**
     * 清空历史
     */
    public void deleteAllRecord() {
        getRealm().beginTransaction();
        getRealm().delete(Record.class);
        getRealm().commitTransaction();
    }

    /**
     * 增加 搜索记录
     *
     * @param bean
     */
    public void insertSearchHistory(SearchKey bean) {
        //如果有不保存
        List<SearchKey> list = getSearchHistoryList(bean.getSearchKey());
        if (list == null || list.size() == 0) {
            getRealm().beginTransaction();
            getRealm().copyToRealm(bean);
            getRealm().commitTransaction();
        }
        //如果保存记录超过20条，就删除一条。保存最多20条
        List<SearchKey> listAll = getSearchHistoryListAll();
        if (listAll != null && listAll.size() >= 10) {
            deleteSearchHistoryList(listAll.get(listAll.size() - 1).getSearchKey());
        }
    }

    /**
     * 获取搜索历史记录列表
     *
     * @return
     */
    public List<SearchKey> getSearchHistoryList(String value) {
        //使用findAllSort ,先findAll再result.sort排序
        RealmResults<SearchKey> results = getRealm().where(SearchKey.class).contains("searchKey", value).findAll().sort("insertTime", Sort.DESCENDING);
        return getRealm().copyFromRealm(results);
    }

    /**
     * 删除指定搜索历史记录列表
     *
     * @return
     */
    public void deleteSearchHistoryList(String value) {
        SearchKey data = getRealm().where(SearchKey.class).equalTo("searchKey", value).findFirst();
        getRealm().beginTransaction();
        data.deleteFromRealm();
        getRealm().commitTransaction();
    }

    public void deleteSearchHistoryAll() {
        getRealm().beginTransaction();
        getRealm().delete(SearchKey.class);
        getRealm().commitTransaction();
    }

    /**
     * 获取搜索历史记录列表
     *
     * @return
     */
    public List<SearchKey> getSearchHistoryListAll() {
        //使用findAllSort ,先findAll再result.sort排序
        RealmResults<SearchKey> results = getRealm().where(SearchKey.class).findAll().sort("insertTime", Sort.DESCENDING);
        return getRealm().copyFromRealm(results);
    }
}
