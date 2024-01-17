package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.task10.handlers.impl.GetReservationsHandler;
import com.task10.handlers.impl.GetTableByIdHandler;
import com.task10.handlers.impl.GetTablesHandler;
import com.task10.handlers.impl.PostReservationsHandler;
import com.task10.handlers.impl.PostTableHandler;
import com.task10.handlers.impl.SigninHandler;
import com.task10.handlers.impl.SignupHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class HandlersRegistry {

	private List<HttpEventHandler> handlers = new ArrayList<>();

	{
		handlers.add(new GetTableByIdHandler());
		handlers.add(new GetTablesHandler());
		handlers.add(new PostTableHandler());
		handlers.add(new PostReservationsHandler());
		handlers.add(new GetReservationsHandler());
		handlers.add(new SignupHandler());
		handlers.add(new SigninHandler());
	}

	public Optional<HttpEventHandler> getHandler(APIGatewayProxyRequestEvent event) {
		return handlers.stream()
					   .filter(handler -> handler.isEventSupported(event))
					   .findFirst();
	}
}
