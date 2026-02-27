The issues reported by DQM can be viewed/editted on this page: <br>
    <a href="${deepLink}">${deepLink}</a>

<br><br>
<#list dqmIssues?keys as key>
<p>
<b>${key}</b>
<table>
    <tr>
        <th>NC12</th>
        <th>Site</th>
        <th>MAG</th>
        <th>Description</th>
    </tr>
    <#list dqmIssues[key] as rows>
    <tr>
        <#list rows as elem>
        <td>${elem}</td>
        </#list>
    </tr>
    </#list>
</table>
<br>
</#list>
