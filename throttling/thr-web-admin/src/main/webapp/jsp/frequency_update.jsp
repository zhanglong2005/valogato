<%@ taglib prefix="s" uri="/struts-tags" %>
<link rel="stylesheet" href="./css/throttling.css" type="text/css"/>

<div id="content_header">The frequency of the usage of backend services / List / Update (
<s:label value="%{nameBackendService}" theme="simple"/>)</div>

<s:form action="save_frequency.action" method="post" theme="simple" validate="true">
   <s:hidden value="%{nameBackendService}" name="nameBackendService" />

   <table class="edit_background">
      <tr>
         <td>Backend service:</td>
         <td>
            <div style="width: 600px; word-wrap: break-word">
               <b><s:label value="%{nameBackendService}"/></b>
            </div>
         </td>
      </tr>
      <tr>
         <td>Frequency:</td>
         <td>
            <s:textfield name="frequency" size="6" maxlength="6"/>
         </td>
      </tr>
      <tr>
         <td>Number of sleeping requests:</td>
         <td>
            <s:textfield name="sleepingRequests" size="6" maxlength="6"/>
         </td>
      </tr>
      <tr>
         <td colspan="2">
            <font color="red"><s:fielderror/></font>
         </td>
      </tr>
      <tr>
         <td>
            <s:submit name="buttonName" value="Save" align="center" theme="simple"/>
            <s:submit name="buttonName" value="Cancel" align="center" theme="simple"/>
         </td>
      </tr>
   </table>
   
</s:form>