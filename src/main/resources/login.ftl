<#-- @ftlvariable name="error" type="java.lang.String" -->
<#-- @ftlvariable name="redirect" type="java.lang.String" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Вход</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.15.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"></script>
</head>
<body>

<div class="container">
    <div class="col-md-6" style="float: none; margin: 0 auto;">
        <div>
            <h3>Вход</h3>
            <#if error??>
                <h4 class="text-danger">${error}</h4>
            </#if>
            <form method="post" action="/login.html">
                <#if redirect??>
                    <input type="hidden" value="${redirect}" name="redirect">
                </#if>
                <div class="form-group">
                    <input class="form-control" placeholder="Логин" name="login">
                </div>
                <div class="form-group">
                    <input type="password" class="form-control" placeholder="Пароль" name="password">
                </div>
                <button type="submit" class="btn-primary form-control">Вход</button>
            </form>
        </div>
    </div>
</div>

</body>
</html>
