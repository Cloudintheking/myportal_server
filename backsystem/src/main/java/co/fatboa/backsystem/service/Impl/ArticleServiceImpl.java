package co.fatboa.backsystem.service.Impl;

import co.fatboa.backsystem.dao.IArticleDao;
import co.fatboa.backsystem.dao.ICategoryDao;
import co.fatboa.backsystem.domain.dto.ArticleDto;
import co.fatboa.backsystem.domain.entity.Article;
import co.fatboa.backsystem.domain.entity.Category;
import co.fatboa.backsystem.domain.entity.Zone;
import co.fatboa.backsystem.domain.mapper.ArticleMapper;
import co.fatboa.backsystem.domain.params.ArticleParam;
import co.fatboa.backsystem.service.IArticleService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Auther: hl
 * @Date: 2018/9/2 11:58
 * @Description:
 * @Modified By:
 * @Version 1.0
 */
@Service
public class ArticleServiceImpl implements IArticleService {
    @Autowired
    private IArticleDao articleDao;
    @Autowired
    private ICategoryDao categoryDao;
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @Override
    public ArticleDto save(ArticleDto dto) throws Exception {
        Article article = articleMapper.to(dto);
        if (dto.getCategory() == null && dto.getCategory().trim().isEmpty()) {
            throw new Exception("新增文章时，必须关联栏目id");
        } else {
            Category category = this.categoryDao.findById(dto.getCategory().trim());
            if (category == null) {
                throw new Exception("关联兰id:" + dto.getCategory().trim() + "不存在");
            }
            article.setCategory(category);
        }
        article.setId(null);//防止前端误传id
        article.setDate(new Date());
        this.articleDao.save(article);
        return this.articleMapper.from(article);
    }

    /**
     * 单个查询
     *
     * @param params
     * @return
     */
    @Override
    public Article findOne(ArticleParam params) {
        return this.articleDao.findOne(queryPackage(params));
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Article findById(String id) throws Exception {
        Article article = this.articleDao.findById(id);
        if (article == null) {
            throw new Exception("找不到id=" + id + "的文章");
        }
        return article;
    }

    /**
     * 分页查询
     *
     * @param params
     * @return
     */
    @Override
    public Page<Article> findByPage(ArticleParam params) {
        Integer pageIndex = 0;
        Integer pageSize = 10;
        Query query = queryPackage(params);
        if (params.getPageIndex() != null) {
            pageIndex = params.getPageIndex();
        }
        if (params.getPageSize() != null) {
            pageSize = params.getPageSize();
        }
        Pageable pageable = new PageRequest(pageIndex, pageSize);
        long count = this.articleDao.count(query);
        List<Article> articles = this.articleDao.findAll(query.with(pageable));
        return new PageImpl<Article>(articles, pageable, count);
    }

    /**
     * 参数查询
     *
     * @param params
     * @return
     */
    @Override
    public List<Article> findAll(ArticleParam params) {
        return this.articleDao.findAll(queryPackage(params));
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void delete(String... ids) throws Exception {
        this.articleDao.delete(ids);
    }

    /**
     * 单删
     *
     * @param id
     */
    @Override
    public void delete(String id) throws Exception {
        this.articleDao.delete(id);
    }

    /**
     * 更新
     *
     * @param dto
     */
    @Override
    public void update(ArticleDto dto) throws Exception {
        Query query = new Query();
        if (dto.getId() != null && !dto.getId().trim().isEmpty()) {
            if (this.articleDao.findById(dto.getId()) == null) {
                throw new Exception("不存在该文章id:" + dto.getId() + ",无法更新");
            } else {
                query.addCriteria(Criteria.where("id").is(new ObjectId(dto.getId())));
            }
        } else {
            throw new Exception("更新文章,请务必上传文章id");
        }
        Update update = new Update();
        if (dto.getCategory() != null && !dto.getCategory().trim().isEmpty()) {
            Category category = this.categoryDao.findById(dto.getCategory());
            if (category == null) {
                throw new Exception("不存在该父级栏目id:" + dto.getCategory());
            }
            update.set("category", category);
        } else {
            throw new Exception("文章关联栏目id不能为空");
        }
        if (dto.getTitle() != null) {
            update.set("title", dto.getTitle());
        }
        if (dto.getContent() != null) {
            update.set("content", dto.getContent());
        }
        if (dto.getShow() != null) {
            update.set("show", dto.getShow());
        }
        if (dto.getCover() != null) {
            update.set("cover", dto.getCover());
        }
        this.articleDao.update(query, update);
    }

    /**
     * 封装query
     *
     * @param param
     * @return
     */
    Query queryPackage(ArticleParam param) {
        Query query = new Query();
        Sort sort = null;
        Criteria criteria = new Criteria();
        if (param.getId() != null) {
            criteria.and("id").is(new ObjectId(param.getId()));
        }
        if (param.getCategory() != null && !param.getCategory().trim().isEmpty()) {
            criteria.and("category.$id").is(new ObjectId(param.getCategory().trim()));
        }
        if (param.getTitle() != null) {
            criteria.and("title").regex(param.getTitle());
        }
        if (param.getShow() != null) {
            criteria.and("show").is(param.getShow());
        }
        if (param.getStartDate() != null) {
            criteria.and("date").gte(param.getStartDate());
        }
        if (param.getEndDate() != null) {
            criteria.and("date").lte(param.getEndDate());
        }
        if (param.getSortDirection() != null && param.getSortFiled() != null) {
            if (param.getSortDirection().equals("desc")) {
                sort = new Sort(Sort.Direction.DESC, param.getSortFiled());
                query.with(sort);
            } else if (param.getSortDirection().equals("desc")) {
                sort = new Sort(Sort.Direction.DESC, param.getSortFiled());
                query.with(sort);
            }
        }
        return query;
    }
}
