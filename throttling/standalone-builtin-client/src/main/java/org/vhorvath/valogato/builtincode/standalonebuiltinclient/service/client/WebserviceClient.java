package org.vhorvath.valogato.builtincode.standalonebuiltinclient.service.client;

import javax.xml.ws.BindingProvider;

import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.core.controller.ThrottlingProcessController;

import vhorvath.throttling.simulation.wsdl.get_customer_by_id.GetByIdFault_Exception;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id.GetByIdRequest;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id.GetByIdResponse;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id.GetCustomerById;
import vhorvath.throttling.simulation.wsdl.get_customer_by_id.ThrottlingSimulatedPortType;


public class WebserviceClient implements ISimulatedService<GetByIdRequest, GetByIdResponse, GetByIdFault_Exception> {


	private String endpoint = null;
	private String requestId = null;
	
	
	public void call() {
		System.out.println("Call!");
		
		GetByIdRequest request = createGetByIdRequest();
		
		IThrottlingController<GetByIdRequest, GetByIdResponse, GetByIdFault_Exception> controller = 
				new ThrottlingProcessController<GetByIdRequest, GetByIdResponse, GetByIdFault_Exception>();
		String nameBackendService = "BigSystem";
		String simulatedServiceName = "BigSimulatedSystem_Waiting";
		setEndpoint("http://localhost:8028/mockGetCustomerByIdHttpBinding");
		requestId = controller.getRequestId();
		
		try {
			controller.processRequest(request, this, nameBackendService, simulatedServiceName);
		} catch (GetByIdFault_Exception e) {
			System.out.println("Error at calling the service!");
			e.printStackTrace();
		}
	}


	private GetByIdRequest createGetByIdRequest() {
		GetByIdRequest request = new GetByIdRequest();
		request.setId("1111");
		
		return request;
	}


	/*
	 * *********************************** Methods for the simulated service *********************************
	 */
	public GetByIdResponse forwardRequest(GetByIdRequest req) throws GetByIdFault_Exception {
		// get the port
		GetCustomerById service = new GetCustomerById(/*wsdlLocation */);
		ThrottlingSimulatedPortType port = service.getThrottlingSimulatedPort();
		
		// set the endpoint
		BindingProvider bp = (BindingProvider)port;
		bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
		
		System.out.println("### Calling the service...");
		GetByIdResponse response = port.getById(req);
		System.out.println("### Called the service!");
		
		return response;
	}

	public GetByIdFault_Exception buildFault(String reason) {
		return new GetByIdFault_Exception(String.format("Fault from simulated service: The backend service cannot be called! - request id: %s, reason: %s", 
				requestId, reason));
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}