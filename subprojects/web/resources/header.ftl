<#-- @ftlvariable name="skipReports" type="boolean" -->
<#-- @ftlvariable name="login" type="dtrack.web.actions.LoginInfo" -->
<#-- @ftlvariable name="project" type="dtrack.web.actions.ProjectInfo" -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="navbar-collapse collapse">
        <ul class="navbar-nav mr-auto">
            <#if project??>
            <li class="nav-item">
                <span class="navbar-text">Проект:</span>
            </li>
            <li class="nav-item active dropdown mr-3">
                <a class="nav-link dropdown-toggle" href="#" data-toggle="dropdown">${project.projectName}</a>
                <div class="dropdown-menu">
                    <#list project.availableProjects as p>
                        <a class="dropdown-item" href="${p.viewLink}">${p.name}</a>
                    </#list>
                </div>
            </li>
            <li class="nav-item active dropdown mr-3">
                <a class="nav-link dropdown-toggle" href="#" data-toggle="dropdown">Просмотр багов</a>
                <div class="dropdown-menu">
                    <a class="dropdown-item" href="${project.reportRootUrl}">Все отчеты</a>
                    <#if !skipReports??>
                    <#list project.favourites as fav>
                        <a class="dropdown-item" href="${fav.viewLink}">${fav.title}</a>
                    </#list>
                    </#if>
                </div>
            </li>
            <li class="nav-item active">
                <form style="display: inline;" method="get" action="${project.newBugUrl}">
                    <button class="btn btn-primary">Новый баг</button>
                </form>
            </li>
            <#else>
            <li class="nav-item">
                <span class="navbar-text">Выбор проекта</span>
            </li>
            </#if>
        </ul>
        <ul class="navbar-nav">
            <li class="nav-item active mr-4">
                <span class="navbar-text">Пользователь: ${login.displayUser}</span>
            </li>
            <li class="nav-item active">
                <form method="post" action="logout.html">
                    <button type="submit" class="btn btn-secondary">Выход</button>
                </form>
            </li>
        </ul>
    </div>
</nav>
