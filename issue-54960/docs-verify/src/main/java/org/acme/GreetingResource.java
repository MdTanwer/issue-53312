package org.acme;

import java.util.List;

import org.acme.users.Gift;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/gifts")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.APPLICATION_JSON)
public class GreetingResource {

    @Inject
    GiftService giftService;

    @GET
    public List<Gift> list() {
        return giftService.findAll();
    }

    @POST
    public Response create(String giftName) {
        Gift gift = giftService.createGift(giftName);
        return Response.status(Response.Status.CREATED)
                .entity(gift)
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (!giftService.deleteGift(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.noContent().build();
    }
}
