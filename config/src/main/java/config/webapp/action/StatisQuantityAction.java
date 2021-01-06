package config.webapp.action;

import com.riozenc.titanTool.common.json.utils.GsonUtils;
import com.riozenc.titanTool.spring.web.http.HttpResult;
import com.riozenc.titanTool.spring.web.http.HttpResultPagination;
import config.webapp.domain.CommonParamDomain;
import config.webapp.service.ICommonParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            Long customerSize = (long) redisTemplate.opsForValue().get(customerSizeKey);
            String transformerSizeKey = "transformerSize";
            Long transformerSize = (long) redisTemplate.opsForValue().get(transformerSizeKey);
            String meterAssetsSizeKey = "meterAssetsSize";
            Long meterAssetsSize = (long) redisTemplate.opsForValue().get(meterAssetsSizeKey);
            String writeSectSizeKey = "writeSectSize";
            Long writeSectSize = (long) redisTemplate.opsForValue().get(writeSectSizeKey);
            String subSizeKey = "subSize";
            Long subSize = (long) redisTemplate.opsForValue().get(subSizeKey);
            String meterSizeKey = "meterSize";
            Long meterSize = (long) redisTemplate.opsForValue().get(meterSizeKey);

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
        Map<String, Long> returnMap = new HashMap<>();
  //下拉
        CommonParamDomain systemCommonConfigDomain =
                new CommonParamDomain();
        systemCommonConfigDomain.setPageSize(-1);
        systemCommonConfigDomain.setType("ELEC_TYPE");
        List<CommonParamDomain> systemCommonConfigDomains =
                commonParamService.findByWhere(systemCommonConfigDomain);
        Map<Integer, String> systemCommonMap = systemCommonConfigDomains.stream()
                .collect(Collectors.toMap(CommonParamDomain::getParamKey, m -> m.getParamValue(), (k1, k2) -> k1));
        try {
            String customerSizeKey = "customerSize";
            Long customerSize = (long) redisTemplate.opsForValue().get(customerSizeKey);
            String transformerSizeKey = "transformerSize";
            Long transformerSize = (long) redisTemplate.opsForValue().get(transformerSizeKey);
            String meterAssetsSizeKey = "meterAssetsSize";
            Long meterAssetsSize = (long) redisTemplate.opsForValue().get(meterAssetsSizeKey);
            String writeSectSizeKey = "writeSectSize";
            Long writeSectSize = (long) redisTemplate.opsForValue().get(writeSectSizeKey);
            String subSizeKey = "subSize";
            Long subSize = (long) redisTemplate.opsForValue().get(subSizeKey);
            String meterSizeKey = "meterSize";
            Long meterSize = (long) redisTemplate.opsForValue().get(meterSizeKey);

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

}
