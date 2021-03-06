package com.dimple.modules.BackStageModule.VisitorManager.repository;

import com.dimple.modules.BackStageModule.VisitorManager.bean.Visitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

/**
 * @author : Dimple
 * @version : 1.0
 * @class : VisitorRepository
 * @description :
 * @date : 12/26/18 19:15
 */
public interface VisitorRepository extends JpaRepository<Visitor, Integer>, JpaSpecificationExecutor<Visitor> {

    @Query("from Visitor where id=:id")
    Visitor getById(@Param("id") Integer id);

    @Query(value = "select spider as 'name',count(spider) as 'value' from visitor group by spider order by spider limit 0,5", nativeQuery = true)
    List<Map<String, Integer>> getSpiderCount();

    @Query(value = "select count(*) from visitor where date(visit_time)=:date", nativeQuery = true)
    Integer getVisitorCountByVisitTime(@Param("date") String date);

    @Query(value = "select count(*) from visitor where date(visit_time) =:monthDate ", nativeQuery = true)
    long countByMonthVisitTime(@Param("monthDate") String mouthDate);

    @Query(value = "select count(*) as count,ip,os,address,browser from visitor where blacklist = 0 group by ip,os,address,browser order by count desc",
            countQuery = "select count(distinct (ip))from visitor where blacklist=0", nativeQuery = true)
    Page<List<Map<String, String>>> getAllVisitorCount(Pageable pageable);

    @Query("update Visitor set blacklist = :sign where ip=:ip")
    @Modifying
    void setVisitorSign(@Param("ip") String ip, @Param("sign") boolean sign);

    @Query(value = "select count(*) from visitor where ip=:ip", nativeQuery = true)
    Integer getVisitorCountByIp(String ip);
}
