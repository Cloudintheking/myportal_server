package co.fatboa.myportal.service.impl;

import co.fatboa.myportal.dao.IFileDao;
import co.fatboa.myportal.domain.dto.FileDTO;
import co.fatboa.myportal.domain.params.FileParams;
import co.fatboa.myportal.service.IFileService;
import co.fatboa.myportal.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: hl
 * @Description: 文件服务实现
 * @Date: 18:04 2018/8/25
 * @Modified By:
 * @Version 1.0
 */
@Service
public class FileServiceImpl implements IFileService {
    @Autowired
    private IFileDao fileDao;


    /**
     * 上传文件
     *
     * @param files
     * @return
     */
    @Override

    public List<Map> save(MultipartFile... files) throws IOException {
        List<Map> gridFSFiles = new ArrayList<>();
        GridFSFile gfile;
        for (MultipartFile file : files) {
            Map<String, Object> map = new HashMap<>();
            String md5 = FileUtils.getFileMD5(file.getInputStream());
            GridFSDBFile gb = this.fileDao.findOne(Query.query(Criteria.where("md5").is(md5)));
            if (gb == null) { // 文件不存在
                gfile = this.fileDao.store(file.getInputStream(), file.getOriginalFilename());
                map.put("id", gfile.getId().toString());
            } else { //文件已存在
                map.put("id", gb.getId().toString());
            }
            gridFSFiles.add(map);
        }
        return gridFSFiles;
    }

    /**
     * 查询单个文件
     *
     * @param fileParams
     * @return
     */
    @Override
    public GridFSDBFile findOne(FileParams fileParams) throws FileNotFoundException {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String json = gson.toJson(fileParams);
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = gson.fromJson(json, type);
        for (String key : map.keySet()) {
            criteria = criteria.and(key).is(map.get(key));
        }
        GridFSDBFile gb = this.fileDao.findOne(query.addCriteria(criteria));

        if (gb == null)
            throw new FileNotFoundException("文件未找到，查询参数:" + map.toString());
        return gb;
    }

    /**
     * 查询多个文件
     *
     * @param fileParams
     * @return
     */
    @Override
    public List<GridFSDBFile> findAll(FileParams fileParams) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String json = gson.toJson(fileParams);
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        Map<String, Object> map = gson.fromJson(json, type);
        for (String key : map.keySet()) {
            criteria = criteria.and(key).is(map.get(key));
        }
        return this.fileDao.find(query.addCriteria(criteria));
    }

    /**
     * 删除文件
     *
     * @param ids
     */
    @Override
    public void delete(String... ids) throws Exception {
        for (Serializable id : ids) {
            GridFSDBFile gb = this.fileDao.findOne(Query.query(Criteria.where("_id").is(id)));
            if (gb == null) {
                throw new Exception("未找到该文件id：" + id);
            }
            this.fileDao.delete(Query.query(Criteria.where("_id").is(id)));
        }
    }


    public Map toMap(GridFSFile file) {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        String json = gson.toJson(file);
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}
