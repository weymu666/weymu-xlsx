package com.weymu.xlsx.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.weymu.xlsx.common.RequestDTO;
import com.weymu.xlsx.entity.WorkBookEntity;
import com.weymu.xlsx.entity.WorkSheetEntity;
import com.weymu.xlsx.repository.WorkBookRepository;
import com.weymu.xlsx.repository.WorkSheetRepository;
import com.weymu.xlsx.service.MessageProcess;
import com.weymu.xlsx.utils.SheetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.*;
import java.util.*;

/**
 * @author weymu
 * @date 2020/10/28
 * @description
 */
@RestController
public class XLSXController {

    @Autowired
    private WorkBookRepository workBookRepository;

    @Autowired
    private WorkSheetRepository workSheetRepository;

    @Autowired
    private MessageProcess messageProcess;

    @GetMapping("/all")
    public ModelAndView all() {
        List<WorkBookEntity> all = workBookRepository.findAll();
        return new ModelAndView("all", "all", all);
    }

    @GetMapping("/allData")
    public List<WorkBookEntity> all(@PathParam("title") String title) {
        if (title != null) {
            return workBookRepository.findAllByTitle(title);
        } else {
            return workBookRepository.findAll();
        }
    }

    @DeleteMapping("/delById/{wbId}")
    public String delById(@PathVariable(value = "wbId") String wbId) {
        workBookRepository.deleteById(wbId);
        return "1";
    }

    @GetMapping("/create")
    public void create(HttpServletResponse response) throws IOException {
        response.sendRedirect("/" + createId().getStr("id"));
    }

    @GetMapping("/createId")
    public JSONObject createId() {
        WorkBookEntity wb = new WorkBookEntity();
        wb.setName("default");
        JSONObject defautOption = SheetUtil.getDefautOption();
        wb.setOption(defautOption);
        WorkBookEntity saveWb = workBookRepository.save(wb);
        generateSheet(saveWb.getId());
        return new JSONObject() {{
            put("id", saveWb.getId());
            put("option", defautOption);
        }};
    }

    @GetMapping("/{wbId}")
    public ModelAndView index(@PathVariable(value = "wbId") String wbId) {
        Optional<WorkBookEntity> Owb = workBookRepository.findById(wbId);
        WorkBookEntity wb = new WorkBookEntity();
        if (!Owb.isPresent()) {
            wb.setId(wbId);
            wb.setName("default");
            wb.setOption(SheetUtil.getDefautOption());
            WorkBookEntity result = workBookRepository.save(wb);
            generateSheet(wbId);
        } else {
            wb = Owb.get();
        }
        return new ModelAndView("websocket", "wb", wb);
    }

    @PostMapping("/load/{wbId}")
    public String load(@PathVariable(value = "wbId") String wbId) {
        List<WorkSheetEntity> wsList = workSheetRepository.findAllBywbId(wbId);
        List<JSONObject> list = new ArrayList<>();
        wsList.forEach(ws -> {
            list.add(ws.getData());
        });
        return JSONUtil.toJsonStr(list);
    }

    @PostMapping("/loadSheet/{wbId}")
    public String loadSheet(@PathVariable(value = "wbId") String wbId) {
        List<WorkSheetEntity> wsList = workSheetRepository.findAllBywbId(wbId);
        List<JSONObject> list = new ArrayList<>();
        wsList.forEach(ws -> {
            list.add(ws.getData());
        });
        if (!list.isEmpty()) {
            return SheetUtil.buildSheetData(list).toString();
        }
        return SheetUtil.getDefaultAllSheetData().toString();
    }

    @PostMapping("/loadData")
    public String loadData(@RequestBody RequestDTO requestDTO) {
        WorkSheetEntity ws = new WorkSheetEntity();
        List<WorkSheetEntity> allBywbId = workSheetRepository.findAllBywbId(requestDTO.getWbId());
        allBywbId.forEach(dd -> {
            workSheetRepository.deleteById(dd.getId());
        });
        Optional<WorkBookEntity> wb = workBookRepository.findById(requestDTO.getWbId());
        if (wb.isPresent()) {
            WorkBookEntity workBookEntity = wb.get();
            workBookEntity.getOption().put("title", requestDTO.getTitle());
            workBookRepository.save(workBookEntity);
        }
        requestDTO.getDatas().forEach(data -> {
            ws.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            ws.setData(data);
            ws.setWbId(requestDTO.getWbId());
            workSheetRepository.save(ws);
        });
        return "1";
    }

    @PostMapping("/setData")
    public String setData(@RequestBody RequestDTO requestDTO) {
        requestDTO.getDatas().forEach(data -> {
            messageProcess.process(requestDTO.getWbId(), data);
        });
        return "1";
    }

    private void generateSheet(String wbId) {
        SheetUtil.getDefaultSheetData().forEach(jsonObject -> {
            WorkSheetEntity ws = new WorkSheetEntity();
            ws.setWbId(wbId);
            ws.setData(jsonObject);
            ws.setDeleteStatus(0);
            workSheetRepository.save(ws);
        });
    }

}
