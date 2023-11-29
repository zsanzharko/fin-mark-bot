/*
 * Copyright (c) 2023.
 */

package kz.zsanzharko.model;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Data
@Slf4j
public class Report {
    private List<QuestionModel> questionModelList;
    @Getter
    private final SortedSet<String> initials = new TreeSet<>();
    private final SortedMap<Date, Map<String, List<QA>>> report = new TreeMap<>();
    private final Map<String, Map<String, String>> additionReport = new HashMap<>();

    public void addReportByDate(java.sql.Date date, String fullName, QA qa) {
        log.debug("Adding initials {}", fullName);
        initials.add(fullName);
        log.debug("Adding for {} question {}", fullName, qa);
        if (report.containsKey(date)) {
            var usersReport = report.get(date);
            if (!usersReport.containsKey(fullName)) {
                var qaList = new ArrayList<QA>();
                qaList.add(qa);
                usersReport.put(fullName, qaList);
            }
            usersReport.get(fullName).add(qa);
        } else {
            var usersReport = new HashMap<String, List<QA>>();
            var qaList = new ArrayList<QA>();
            qaList.add(qa);
            usersReport.put(fullName, qaList);
            report.put(date, usersReport);
        }
    }

    public void addAdditionReport(String fullName, String key, String value) {
        if (additionReport.containsKey(fullName)) {
            additionReport.get(fullName)
                    .put(key, value);
        } else {
            additionReport.put(fullName, Map.of(key, value));
        }
    }
}

