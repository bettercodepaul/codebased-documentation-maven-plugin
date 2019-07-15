package collectors.models.restapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import annotation.ConsumesAPI;
import collectors.models.InfoObject;
import util.ConsumeDescription;
import util.OfferDescription;

public class CollectedAPIInfoObject extends InfoObject {

	private String serviceName;
	private String serviceTag;
	private APIInfoObject provide;
	private List<ConsumeDescription> consume;

	public CollectedAPIInfoObject(String name) {
		if (name != null) {
			this.serviceName = name;
		} else {
			this.serviceName = ConsumesAPI.DEFAULT_SERVICE;
		}
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceTag() {
		return serviceTag;
	}

	public void setServiceTag(String serviceTag) {
		this.serviceTag = serviceTag;
	}

	public APIInfoObject getProvide() {
		return provide;
	}

	public void setProvide(APIInfoObject provide) {
		this.provide = provide;
	}

	public List<ConsumeDescription> getConsume() {
		return consume;
	}

	public void setConsume(List<ConsumeDescription> consume) {
		this.consume = consume;
	}

	public void addConsumeDescriptionTriple(ConsumeDescription triple) {
		if (this.consume == null) {
			this.consume = new ArrayList<>();
		}
		this.consume.add(triple);
	}

	public void addConsumeDescriptionTriples(Collection<ConsumeDescription> triples) {
		if (this.consume == null) {
			this.consume = new ArrayList<>();
		}
		this.consume.addAll(triples);
	}
	
//	public List<OfferDescription> getProvide() {
//		return provide;
//	}
//
//	public void setProvide(List<OfferDescription> offer) {
//		this.provide = offer;
//	}
//
//	public void addOfferDescriptionTriple(OfferDescription offer) {
//		if (this.provide == null) {
//			this.provide = new ArrayList<>();
//		}
//		this.provide.add(offer);
//	}
//
//	public void addOfferDescriptionTriples(Collection<OfferDescription> offers) {
//		if (this.provide == null) {
//			this.provide = new ArrayList<>();
//		}
//		this.provide.addAll(offers);
//	}

}
