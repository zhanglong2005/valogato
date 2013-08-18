<%@ taglib uri="http://shiro.apache.org/tags" prefix="shiro" %>

<div id="footer_background_gradient">
<table width="99%">
   <tr>
      <td align="left" width="50%">User: <shiro:principal/></td>
      <td align="right" width="50%"><%= new java.text.SimpleDateFormat("HH:mm:ss  dd/MM/yyyy").format(new java.util.Date()) %></td>
   </tr>
</table>
</div>