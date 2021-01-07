package lv.llu.science.bees.webapi.web;

import lv.llu.science.bees.webapi.domain.nodes.DeviceBean;
import lv.llu.science.bees.webapi.domain.nodes.DeviceLogBean;
import lv.llu.science.bees.webapi.domain.nodes.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
public class DeviceController {

	private final NodeService service;

	@Autowired
	public DeviceController(NodeService service) {
		this.service = service;
	}

	@GetMapping
	public List<DeviceBean> listDevices() {
		return service.getDevices();
	}

	@GetMapping(path = "/{id}/events")
	public DeviceLogBean getDeviceEvents(@PathVariable String id) {
		return service.getDeviceEvents(id);
	}

	@PutMapping(path = "/{id}/active")
	public Boolean toggleActiveDevice(@PathVariable String id) {
		return service.toggleActive(id);
	}
}
