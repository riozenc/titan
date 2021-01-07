package config.webapp.action;

import com.google.gson.JsonObject;
import com.riozenc.titanTool.common.json.utils.GsonUtils;
import com.riozenc.titanTool.spring.web.http.HttpResult;
import com.riozenc.titanTool.spring.web.http.HttpResultPagination;
import config.util.MonUtils;
import config.webapp.domain.CommonParamDomain;
import config.webapp.service.ICommonParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//获取首页数量
@ControllerAdvice
@RequestMapping("statisQuantity")
public class StatisQuantityAction {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    @Qualifier("commonParamServiceImpl")
    private ICommonParamService commonParamService;

    // 获取首页明细数量
    @PostMapping("getStatisQuantity")
    @ResponseBody
    public HttpResult getStatisQuantity() {
        Map<String, Long> returnMap = new HashMap<>();
        try {
            String customerSizeKey = "customerSize";
            Long customerSize =
                    (long) Optional.ofNullable(redisTemplate.opsForValue().get(customerSizeKey)).orElse((long)0);
            String transformerSizeKey = "transformerSize";
            Long transformerSize = (long) Optional.ofNullable(redisTemplate.opsForValue().get(transformerSizeKey)).orElse((long)0);
            String meterAssetsSizeKey = "meterAssetsSize";
            Long meterAssetsSize = (long) Optional.ofNullable(redisTemplate.opsForValue().get(meterAssetsSizeKey)).orElse((long)0);
            String writeSectSizeKey = "writeSectSize";
            Long writeSectSize = (long) Optional.ofNullable(redisTemplate.opsForValue().get(writeSectSizeKey)).orElse((long)0);
            String subSizeKey = "subSize";
            Long subSize = (long) Optional.ofNullable(redisTemplate.opsForValue().get(subSizeKey)).orElse((long)0);
            String meterSizeKey = "meterSize";
            Long meterSize = (long) Optional.ofNullable(redisTemplate.opsForValue().get(meterSizeKey)).orElse((long)0);

            returnMap.put("customerSize", customerSize);
            returnMap.put("transformerSize", transformerSize);
            returnMap.put("meterAssetsSize", meterAssetsSize);
            returnMap.put("writeSectSize", writeSectSize);
            returnMap.put("subSize", subSize);
            returnMap.put("meterSize", meterSize);
        } catch (Exception e) {
            return new HttpResult(HttpResult.ERROR,
                    "获取首页数量失败" + e.getMessage());
        }

        return new HttpResult(HttpResult.SUCCESS, returnMap);
    }

    // 获取用电分类数量
    @PostMapping("getElecTypeQuantity")
    @ResponseBody
    public HttpResult getElecTypeQuantity() {
        //下拉
        CommonParamDomain systemCommonConfigDomain =
                new CommonParamDomain();
        systemCommonConfigDomain.setPageSize(-1);
        systemCommonConfigDomain.setType("ELEC_TYPE");
        List<CommonParamDomain> systemCommonConfigDomains =
                commonParamService.findByWhere(systemCommonConfigDomain);
        Map<Integer, String> systemCommonMap = systemCommonConfigDomains.stream()
                .collect(Collectors.toMap(CommonParamDomain::getParamKey, m -> m.getParamValue(), (k1, k2) -> k1));

        List<Map<String,String>> mapList=new ArrayList<>();
        try {
            systemCommonMap.forEach((k,v)->{
                Long num =
                        (long) Optional.ofNullable(redisTemplate.opsForValue().get("METER_ELEC_TYPE"+k)).orElse((long)0);
                if(num>0){
                    Map<String,String> map=new HashMap<>();
                    map.put("name",v);
                    map.put("value",num.toString());
                    mapList.add(map);
                }

            });
        } catch (Exception e) {
            return new HttpResult(HttpResult.ERROR,
                    "获取首页饼形图数量失败" + e.getMessage());
        }

        return new HttpResult(HttpResult.SUCCESS, mapList);
    }


    // 获取电量
    @PostMapping("getPowerQuantity")
    @ResponseBody
    public HttpResult getPowerQuantity() throws Exception {

        String currentmon=commonParamService.getCurrentMon();
        String endMon=MonUtils.getNextMon(currentmon.substring(0, 4)+"12");
        String startMon=currentmon.substring(0, 4)+"01";

        Map<String,String> returnMap=new TreeMap<>();
        try {
        while (!startMon.equals(endMon)){
            Double power =
                    (double) Optional.ofNullable(redisTemplate.opsForValue().get("POWER_"+startMon)).orElse((double)0);
            returnMap.put(startMon,power.toString());
            startMon= MonUtils.getNextMon(startMon);
        }
        } catch (Exception e) {
            return new HttpResult(HttpResult.ERROR,
                    "获取首页电量数量失败" + e.getMessage());
        }
        return new HttpResult(HttpResult.SUCCESS, returnMap);
    }

    // 获取电量
    @PostMapping("getMoneyQuantity")
    @ResponseBody
    public HttpResult getMoneyQuantity() throws Exception {

        String currentmon=commonParamService.getCurrentMon();
        String endMon=MonUtils.getNextMon(currentmon.substring(0, 4)+"12");
        String startMon=currentmon.substring(0, 4)+"01";

        Map<String,String> returnMap=new TreeMap<>();
        try {
            while (!startMon.equals(endMon)){
                Double amount =
                        (double) Optional.ofNullable(redisTemplate.opsForValue().get("AMOUNT_"+startMon)).orElse((double)0);
                returnMap.put(startMon,amount.toString());
                startMon= MonUtils.getNextMon(startMon);
            }
        } catch (Exception e) {
            return new HttpResult(HttpResult.ERROR,
                    "获取首页电费数量失败" + e.getMessage());
        }

        return new HttpResult(HttpResult.SUCCESS, returnMap);
    }

}
