package org.example.rulemanager.controller;

import org.example.rulemanager.entity.TableProcess;
import org.example.rulemanager.repository.TableProcessRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TableProcessController {

    private final TableProcessRepository repository;

    public TableProcessController(TableProcessRepository repository) {
        this.repository = repository;
    }

    @GetMapping({"/", "/rules"})
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        model.addAttribute("rules", repository.findAll(keyword));
        model.addAttribute("keyword", keyword);

        return "rules";
    }

    @GetMapping("/rules/new")
    public String newRule(Model model) {
        TableProcess rule = new TableProcess();
        rule.setOperateType("insert");
        rule.setSinkType("hbase");
        rule.setSinkPk("id");

        model.addAttribute("rule", rule);
        model.addAttribute("edit", false);

        return "rule-form";
    }

    @PostMapping("/rules")
    public String create(TableProcess rule, RedirectAttributes redirectAttributes) {
        normalize(rule);

        repository.insert(rule);

        redirectAttributes.addFlashAttribute("msg", "新增规则成功");
        return "redirect:/rules";
    }

    @GetMapping("/rules/edit")
    public String edit(@RequestParam("sourceTable") String sourceTable,
                       @RequestParam("operateType") String operateType,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        TableProcess rule = repository.findById(sourceTable, operateType);

        if (rule == null) {
            redirectAttributes.addFlashAttribute("msg", "规则不存在");
            return "redirect:/rules";
        }

        model.addAttribute("rule", rule);
        model.addAttribute("edit", true);
        model.addAttribute("oldSourceTable", sourceTable);
        model.addAttribute("oldOperateType", operateType);

        return "rule-form";
    }

    @PostMapping("/rules/update")
    public String update(TableProcess rule,
                         @RequestParam("oldSourceTable") String oldSourceTable,
                         @RequestParam("oldOperateType") String oldOperateType,
                         RedirectAttributes redirectAttributes) {

        normalize(rule);

        repository.update(rule, oldSourceTable, oldOperateType);

        redirectAttributes.addFlashAttribute("msg", "修改规则成功");
        return "redirect:/rules";
    }

    @PostMapping("/rules/delete")
    public String delete(@RequestParam("sourceTable") String sourceTable,
                         @RequestParam("operateType") String operateType,
                         RedirectAttributes redirectAttributes) {

        repository.delete(sourceTable, operateType);

        redirectAttributes.addFlashAttribute("msg", "删除规则成功");
        return "redirect:/rules";
    }

    private void normalize(TableProcess rule) {
        if (!StringUtils.hasText(rule.getSinkPk())) {
            rule.setSinkPk("id");
        }

        if (!StringUtils.hasText(rule.getSinkExtend())) {
            rule.setSinkExtend(null);
        }
    }
}