package com.preetinest.impl;

import com.preetinest.dto.response.MenuResponseDTO;
import com.preetinest.entity.Blog;
import com.preetinest.entity.Clients;
import com.preetinest.entity.Services;
import com.preetinest.repository.BlogRepository;
import com.preetinest.repository.ClientRepository;
import com.preetinest.repository.ServiceRepository;
import com.preetinest.service.MenuService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private final ServiceRepository serviceRepository;
    private final BlogRepository blogRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public MenuServiceImpl(ServiceRepository serviceRepository, BlogRepository blogRepository,
                           ClientRepository clientRepository) {
        this.serviceRepository = serviceRepository;
        this.blogRepository = blogRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public Map<String, Object> fetchAllMenuItems() {
        MenuResponseDTO responseDTO = new MenuResponseDTO();

        // Fetch active services
        List<Map<String, Object>> serviceItems = serviceRepository.findAllActiveServices()
                .stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());

        // Fetch active blogs
        List<Map<String, Object>> blogItems = blogRepository.findAllActiveBlogs()
                .stream()
                .map(this::mapBlogToResponse)
                .collect(Collectors.toList());

        // Fetch active clients
        List<Map<String, Object>> clientItems = clientRepository.findAllActiveClients()
                .stream()
                .map(this::mapClientToResponse)
                .collect(Collectors.toList());

        // Check if any items are present
        if (serviceItems.isEmpty() && blogItems.isEmpty() && clientItems.isEmpty()) {
            throw new EntityNotFoundException("No menu items found");
        }

        responseDTO.setServices(serviceItems);
        responseDTO.setBlogs(blogItems);
        responseDTO.setClients(clientItems);

        Map<String, Object> response = new HashMap<>();
        response.put("services", responseDTO.getServices());
        response.put("blogs", responseDTO.getBlogs());
        response.put("clients", responseDTO.getClients());
        response.put("whoWeAre", responseDTO.getWhoWeAre());
        return response;
    }

    private Map<String, Object> mapServiceToResponse(Services service) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", service.getId());
        item.put("name", service.getName());
        item.put("slug", service.getSlug());
        return item;
    }

    private Map<String, Object> mapBlogToResponse(Blog blog) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", blog.getId());
        item.put("title", blog.getTitle());
        item.put("slug", blog.getSlug());
        return item;
    }

    private Map<String, Object> mapClientToResponse(Clients client) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", client.getId());
        item.put("name", client.getName());
        item.put("slug", client.getSlug());
        return item;
    }


}