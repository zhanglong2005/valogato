<%@ taglib prefix="s" uri="/struts-tags" %>

<table>
   <tr>
      <td><b>Feature:</b></td>
      <td>
         <s:select label="Features" list="features" onchange="submitForm()" listKey="key" listValue="value" 
                   value="%{selectedFeature}" name="selectedFeature" />
      </td>
   </tr>
   <s:if test="featureParams != null && featureParams.size > 0">
      <tr>
         <td><u>Parameters</u></td>
      </tr>
      <s:iterator value="featureParams" status="i" var="feature">
         <s:hidden name="featureParams[%{#i.index}].name"/>
         <tr>
            <td>
               <s:label value="%{#feature.title}"/>:
            </td>
            <td>
               <s:textfield value="%{#feature.value}" name="featureParams[%{#i.index}].value" label="%{#feature.title}" size="50" maxlength="2000" />
            </td>
         </tr>
      </s:iterator>
   </s:if>
</table>