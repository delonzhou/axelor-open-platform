package com.axelor.web.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.service.MetaService;
import com.axelor.rpc.Request;
import com.axelor.rpc.Response;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/rest/{model}")
public class RestService extends ResourceService {
	
	@Inject
	private MetaService service;
	
	@GET
	public Response find(
			@QueryParam("limit")
			@DefaultValue("40") int limit,
			@QueryParam("offset")
			@DefaultValue("0") int offset,
			@QueryParam("q") String query) {
		
		Request request = new Request();
		request.setOffset(offset);
		request.setLimit(limit);
		return getResource().search(request);
	}
	
	@POST
	@Path("search")
	public Response find(Request request) {
		request.setModel(getModel());
		return getResource().search(request);
	}
	
	@POST
	public Response save(Request request) {
		request.setModel(getModel());
		return getResource().save(request);
	}
	
	@PUT
	public Response create(Request request) {
		request.setModel(getModel());
		return getResource().save(request);
	}
	
	@GET
	@Path("{id}")
	public Response read(
			@PathParam("id") long id) {
		return getResource().read(id);
	}
	
	@POST
	@Path("{id}/fetch")
	public Response fetch(
			@PathParam("id") long id, Request request) {
		return getResource().fetch(id, request);
	}
	
	@POST
	@Path("{id}")
	public Response update(@PathParam("id") long id, Request request) {
		request.setModel(getModel());
		return getResource().save(request);
	}
	
	@DELETE
	@Path("{id}")
	public Response delete(@PathParam("id") long id, @QueryParam("version") int version) {
		Request request = new Request();
		request.setModel(getModel());
		request.setData(ImmutableMap.of("id", (Object) id, "version", version));
		return getResource().remove(id, request);
	}
	
	@POST
	@Path("{id}/remove")
	public Response remove(@PathParam("id") long id, Request request) {
		request.setModel(getModel());
		return getResource().remove(id, request);
	}
	
	@POST
	@Path("removeAll")
	public Response remove(Request request) {
		request.setModel(getModel());
		return getResource().remove(request);
	}

	@GET
	@Path("{id}/copy")
	public Response copy(@PathParam("id") long id) {
		return getResource().copy(id);
	}
	
	@GET
	@Path("{id}/details")
	public Response details(@PathParam("id") long id) {
		Request request = new Request();
		Map<String, Object> data = new HashMap<String, Object>();
		
		data.put("id", id);
		request.setModel(getModel());
		request.setData(data);
		
		return getResource().getRecordName(request);
	}

	@POST
	@Path("upload")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(
			@FormDataParam("request") FormDataBodyPart requestText,
			@FormDataParam("field") String field,
			@FormDataParam("file") InputStream fileStream,
			@FormDataParam("file") FormDataContentDisposition fileDetails) throws IOException {

		requestText.setMediaType(MediaType.APPLICATION_JSON_TYPE);

		Request request = requestText.getEntityAs(Request.class);
		request.setModel(getModel());

		Map<String, Object> values = request.getData();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = fileStream.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}

		values.put(field, out.toByteArray());

		return getResource().save(request);
	}
	
	@GET
	@Path("{id}/{field}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@SuppressWarnings("all")
	public javax.ws.rs.core.Response download(
			@PathParam("id") Long id,
			@PathParam("field") String field) {
		
		Class klass = getResource().getModel();
		Mapper mapper = Mapper.of(klass);
		Model bean = JPA.find(klass, id);
		Property prop = mapper.getNameField();
		Object data = mapper.get(bean, field);
		String name = getModel() + "_" + field;
		if(prop != null){
			name = prop.get(bean) != null ? prop.get(bean).toString() : name;
			if(!prop.getName().equals("fileName")){
				name = name.replaceAll("\\s", "") + "_" + id;
				name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
			}
		}
		
		if (data == null) {
			return javax.ws.rs.core.Response.noContent().build();
		}
		return javax.ws.rs.core.Response.ok(data).header("Content-Disposition", "attachment; filename=" + name).build();
	}
	
	@POST
	@Path("{id}/attachment")
	public Response attachment(@PathParam("id") long id, Request request){
		return service.getAttachment(id, getModel(), request);
	}
	
	@POST
	@Path("removeAttachment")
	public Response removeAttachment(Request request) {
		request.setModel(getModel());
		return service.removeAttachment(request);
	}
	
	@POST
	@Path("{id}/addAttachment")
	public Response addAttachment(@PathParam("id") long id, Request request) {
		request.setModel(getModel());
		return service.addAttachment(id, request);
	}

	@POST
	@Path("verify")
	public Response verify(Request request) {
		request.setModel(getModel());
		return getResource().verify(request);
	}

	@GET
	@Path("perms")
	public Response perms(@QueryParam("action") String action, @QueryParam("id") Long id) {
		if (action != null && id != null) {
			return getResource().perms(id, action);
		}
		if (id != null) {
			return getResource().perms(id);
		}
		return getResource().perms();
	}
}