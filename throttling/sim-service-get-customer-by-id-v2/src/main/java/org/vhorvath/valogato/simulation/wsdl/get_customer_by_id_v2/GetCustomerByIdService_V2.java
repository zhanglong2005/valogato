package org.vhorvath.valogato.simulation.wsdl.get_customer_by_id_v2;


import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.core.controller.ThrottlingProcessController;

import vhorvath.throttling.simulation.wsdl.get_customer_by_id_v2.GetByIdFault_Exception;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id_v2.GetByIdRequest;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id_v2.GetByIdResponse;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id_v2.GetCustomerById;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id_v2.ObjectFactory;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id_v2.ThrottlingSimulatedPortType;


/**
 * Sample web service class how to use the Valogato throttling system.
 * 
 * @author Viktor Horvath
 */
@WebService(targetNamespace = "urn:vhorvath:throttling:simulation:wsdl:get-customer-by-id-v2", 
            name = "ThrottlingSimulatedPortType",
            serviceName = "GetCustomerById")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@Remote(ThrottlingSimulatedPortType.class)
@Stateless
public class GetCustomerByIdService_V2 implements ThrottlingSimulatedPortType, 
		ISimulatedService<GetByIdRequest, GetByIdResponse, GetByIdFault_Exception> {

	
	private String endpoint = null;
	private String requestId = null;
	
	private final static Logger logger = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	
	@WebResult(name = "GetByIdResponse", 
			   targetNamespace = "urn:vhorvath:throttling:simulation:wsdl:get-customer-by-id-v2", 
			   partName = "bodyOutput")
	@WebMethod
	public GetByIdResponse getById(@WebParam(partName = "bodyInput",name = "GetByIdRequest",targetNamespace = 
			"urn:vhorvath:throttling:simulation:wsdl:get-customer-by-id-v2") GetByIdRequest request) 
			throws GetByIdFault_Exception {
		
		IThrottlingController<GetByIdRequest, GetByIdResponse, GetByIdFault_Exception> controller = new ThrottlingProcessController<GetByIdRequest, GetByIdResponse, GetByIdFault_Exception>();
		String nameBackendService = "BigSystem";
		String simulatedServiceName = "BigSimulatedSystem_Waiting";
		setEndpoint("http://localhost:8028/mockGetCustomerByIdHttpBinding");
		requestId = controller.getRequestId();
		
		return controller.processRequest(request, this, nameBackendService, simulatedServiceName);
	}

	
	/*
	 * *********************************** Methods for the simulated service *********************************
	 */
	@WebMethod(exclude=true)
	public GetByIdResponse forwardRequest(GetByIdRequest req) throws GetByIdFault_Exception {
		// get the port
		GetCustomerById service = new GetCustomerById(/*wsdlLocation */);
		ThrottlingSimulatedPortType port = service.getThrottlingSimulatedPort();
		
		// set the endpoint
		BindingProvider bp = (BindingProvider)port;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
		
		logger.debug("### Calling the service...");
		GetByIdResponse response = port.getById(req);
		logger.debug("### Called the service!");
		
		return response;
	}

	@WebMethod(exclude=true)
	public GetByIdFault_Exception buildFault(String reason) {
		return new GetByIdFault_Exception(String.format("Fault from simulated service: The backend service cannot be called! - request id: %s, reason: %s", 
				requestId, reason));
	}

	@WebMethod(exclude=true)
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}