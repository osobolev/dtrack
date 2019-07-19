<#-- @ftlvariable name="bug" type="btrack.dao.BugBean" -->
<#-- @ftlvariable name="transitions" type="java.util.List<btrack.dao.TransitionBean>" -->
<#-- @ftlvariable name="attachments" type="java.util.List<btrack.dao.AttachmentBean>" -->
<#-- @ftlvariable name="changes" type="java.util.List<btrack.dao.ChangeListBean>" -->
<#-- @ftlvariable name="users" type="java.util.List<btrack.dao.UserBean>" -->
<#-- @ftlvariable name="error" type="java.lang.String" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>#${bug.bugNum} (${bug.title})</title>
    <#include "head.ftl">
    <link href="summernote-bs4.css" rel="stylesheet">
    <script src="summernote-bs4.js"></script>
    <script src="lang/summernote-ru-RU.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container">
    <div class="card">
        <div class="card-header">
            <h3>#${bug.bugNum}: ${bug.title}</h3>
            <#if error??>
                <h4 class="text-danger">${error}</h4>
            </#if>
            <#-- todo: better layout for header -->
            <div style="float: right;">
                <small>Создан ${bug.created} пользователем ${bug.createdBy}</small>
                <br>
                <small>Изменен ${bug.lastUpdated} пользователем ${bug.lastUpdatedBy}</small>
            </div>
            Приоритет: ${bug.priority}
            <a href="${bug.editLink}">Редактировать</a>
            Статус: ${bug.state}
            <form method="post" action="${bug.assignLink}" id="assignForm">
                <#if bug.assignedUserId??>
                    <input type="hidden" value="${bug.assignedUserId}" name="oldUserId">
                </#if>
                <label for="newUserId">Исполнитель:</label>
                <select name="newUserId" id="newUserId" onchange="onAssignedChange()">
                    <option value=""<#if bug.isNotAssigned()> selected</#if>>не выбран</option>
                    <#list users as u>
                        <option value="${u.id}"<#if bug.isAssigned(u)> selected</#if>>${u.login}</option>
                    </#list>
                </select>
            </form>
            <form id="moveBug" method="post" action="${bug.moveLink}">
                <input type="hidden" name="from" value="${bug.stateId}">
                <#list transitions as t>
                    <button type="submit" name="to" value="${t.toId}" class="btn btn-primary">${t.name}</button>
                </#list>
            </form>
        </div>
        <div class="card-body">
            ${bug.html?no_esc}
        </div>
    </div>
    <#-- todo: attachments layout -->
    <#list attachments as a>
        <a href="${bug.getAttachmentLink(a)}" target="_blank">${a.name}</a>
    </#list>

    <#if changes?has_content>
    <ul class="nav nav-tabs" role="tablist">
        <#list changes as change>
        <li class="nav-item">
            <a class="nav-link<#if change?is_first> active</#if>" data-toggle="tab" href="#${change.id}" role="tab">${change.text}</a>
        </li>
        </#list>
    </ul>

    <div class="tab-content mt-2">
        <#list changes as change>
        <div id="${change.id}" class="tab-pane<#if change?is_first> show active</#if>" role="tabpanel">
            <#list change.changes as c>
            <div>
                <h6 class="change-heading">
                    <#if c.comments?has_content>
                        Комментарий пользователя ${c.user} ${c.ts}
                    <#else>
                        Изменено пользователем ${c.user} ${c.ts}
                    </#if>
                </h6>
                <#list c.comments as cc>
                <div class="change-comment">
                    <#if cc.deleted??>
                        <em>Комментарий удален пользователем ${cc.deleteUser} ${cc.deleted}</em>
                    <#else>
                        <form action="${cc.deleteLink}" method="post">
                            <input type="hidden" name="commentId" value="${cc.id}">
                            <button type="button" class="delete-comment-button" onclick="confirmDeleteComment(event)">Удалить
                            </button>
                        </form>
                        ${cc.commentHtml?no_esc}
                        <#if cc.commentAttachments?has_content>
                            Прикрепленные файлы:
                            <#list cc.commentAttachments as ca>
                                <a href="${bug.getCommentAttachmentLink(ca)}" target="_blank">${ca.name}</a><#if ca_has_next>, </#if>
                            </#list>
                        </#if>
                    </#if>
                </div>
                </#list>
                <ul class="change-other">
                    <#list c.details as cd>
                    <li>
                        <#if cd.fieldChange??>
                            ${cd.fieldChange}
                        <#elseif cd.fileChange??>
                            ${cd.fileChange}
                            <#list cd.changedFiles as cf>
                                <a href="${bug.getAttachmentLink(cf)}" target="_blank">${cf.name}</a><#if cf_has_next>, </#if>
                            </#list>
                        </#if>
                    </li>
                    </#list>
                </ul>
            </div>
            </#list>
        </div>
        </#list>
    </div>
    </#if>

    <a class="btn btn-secondary" data-toggle="collapse" href="#collapseComment" role="button">
        Новый комментарий
    </a>
    <div class="collapse mt-2" id="collapseComment">
        <form method="post" action="${bug.commentLink}" enctype="multipart/form-data">
            <div class="form-group">
                <textarea id="summernote" name="comment"></textarea>
            </div>
            <div class="form-group">
                <div class="custom-file">
                    <input type="file" class="custom-file-input" id="files" name="files" onchange="onFileChange()" multiple>
                    <label class="custom-file-label" for="files" id="filesLabel">Прикрепить файлы</label>
                </div>
            </div>
            <button type="submit" class="btn btn-primary">Добавить комментарий</button>
        </form>
    </div>
</div>

<script>
    $(document).ready(function () {
        configSummer($('#summernote'), '180px');
    });

    function onFileChange() {
        filesChosen($('#files'), $('#filesLabel'));
    }

    function onAssignedChange() {
        $('#assignForm').submit();
    }

    function confirmDeleteComment(e) {
        if (!confirm('Действительно удалить комментарий?'))
            return;
        var form = $(e.target).parent();
        form.submit();
    }

    $('#collapseComment').on('shown.bs.collapse', function () {
        this.scrollIntoView();
    });
</script>

</body>
</html>
