<%@ taglib prefix="s" uri="/struts-tags" %>
<link rel="stylesheet" href="./css/throttling.css" type="text/css"/>

   <!-- ####################### Header ####################### -->
<div id="content_header">(Re)load the configuration</div>

<table class="edit_background" width="600">

   <!-- ####################### (Re)load ####################### -->
   <s:if test="successful != null && !successful.booleanValue()">
      <tr>
         <td colspan="2" valign="top"><font color="red"><b>Unable to load the config file! The error message:</b></font></td>
      </tr>
      <tr>
         <td colspan="2" valign="top">
         	<s:iterator value="configurationErrorMessage" var="message">
         		<b style="color: red">- </b><s:label cssStyle="color: red" value="%{message}" theme="simple"/>
         		<br>
         	</s:iterator>
         </td>
      </tr>
   </s:if>
   <s:if test="successful != null && successful.booleanValue()">
      <tr>
         <td colspan="2" valign="top"><font color="gray">Successful loading.</font></td>
      </tr>
   </s:if>
   <tr>
      <td align="left"><b>Initialization:</b></td>
      <td align="right">
         <s:form action="administration_loadBackendServiceConfig.action" method="post">
            <s:submit name="load" value="Start loading" theme="simple" onclick="return confirm('Are you sure you want to (re)load the data?')"/>
         </s:form>
      </td>
   </tr>

</table>

<br>

<table class="edit_background" width="600">

   <!-- ####################### Load from file ####################### -->
   <s:if test="successfulUpload != null && !successfulUpload.booleanValue()">
      <tr>
         <td colspan="2" valign="top"><font color="red"><b>Unable to load the chosen file! The error message:</b></font></td>
      </tr>
      <tr>
         <td colspan="2" valign="top">
         	<s:iterator value="configurationErrorMessage" var="message">
         		<b style="color: red">- </b><s:label cssStyle="color: red" value="%{message}" theme="simple"/>
         		<br>
         	</s:iterator>
         </td>
      </tr>
   </s:if>
   <s:if test="successfulUpload != null && successfulUpload.booleanValue()">
      <tr>
         <td colspan="2" valign="top"><font color="gray">Successful loading.</font></td>
      </tr>
   </s:if>
   <tr>
      <td colspan="2" align="left" valign="top" style="padding-top:6px"><b>Initialization from external config file:</b></td>
   </tr>
   <tr>
      <td colspan="2" align="right" valign="top">
         <s:form action="administration_uploadNewBackendServiceConfig.action" method="post" enctype="multipart/form-data">
            <s:file name="upload" label="File"/>
            <s:submit onclick="return confirm('Are you sure you want to (re)load the data?')"/>
         </s:form>
      </td>
   </tr>

</table>