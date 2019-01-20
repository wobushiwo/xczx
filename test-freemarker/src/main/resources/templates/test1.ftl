<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Hello World!</title>
</head>
<body>
<#--Hello ${name}!-->
<table>
    <tr>
        <td>序号</td>
        <td>姓名</td>
        <td>年龄</td>
        <td>金额</td>
        <td>出生日期</td>
    </tr>
    ${stus?size}
    <#if stus??>
    <#list stus as stu>
    <tr>
        <td>${stu_index+1}</td>
        <td <#if stu.name == '小明' >bgcolor="red" </#if>>${stu.name}</td>
        <td <#if (stu.age > 18)>bgcolor="pink" </#if>>${stu.age}</td>
        <td>${stu.money}</td>
        <td>${stu.birthday?string("yyyy年MM月dd日")}</td>
    </tr>
    </#list>
    </#if>
</table>
<br/>
姓名：${(stuMap['stu1'].name)!""} <br/>
年龄：${(stuMap['stu1'].age)!""}<br/>
姓名：${(stuMap.stu2.name)!""}<br/>
年龄：${(stuMap.stu2.age)!""}<br/>
<#list stuMap?keys as k>
姓名：${stuMap[k].name} <br/>
年龄：${stuMap[k].age}<br/>
</#list>
<br>
${point?c}
<br>
<#assign text="{'bank':'工商银行','account':'10101920201920212'}" />
<#assign data=text?eval />
开户行：${data.bank} 账号：${data.account}
</body>
</html>