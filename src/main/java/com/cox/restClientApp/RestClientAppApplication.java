package com.cox.restClientApp;

import com.cox.restClientApp.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class RestClientAppApplication {
	private static final Logger log = LoggerFactory.getLogger(RestClientAppApplication.class);
	private static final String HOST_URL = "https://vautointerview.azurewebsites.net/api/";

	public static void main(String[] args) {
		SpringApplication.run(RestClientAppApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	private List<Dealer> getAllDealers(List<Vehicle> vehicles, RestTemplate restTemplate, String dataSetId) {
		List<Dealer> dealers = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		Set<Integer> dealerIds = vehicles.stream().map(v -> v.getDealerId()).collect(Collectors.toSet());
		try {
			List<Future<Dealer>> futures = null;
			List<Callable<Dealer>> callables = new ArrayList<>();
			for (Integer dealerId : dealerIds) {
				//log.info("Calling api for dealerId:"+dealerId);
				Callable<Dealer> callable = () -> restTemplate.getForObject(
						HOST_URL+dataSetId+"/dealers/"+dealerId, Dealer.class);;
				callables.add(callable);
			}

			futures = executorService.invokeAll(callables);

			for (Future<Dealer> future : futures) {
				dealers.add(future.get(4,TimeUnit.SECONDS));
			}

		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		finally {
			executorService.shutdown();;
		}
		return dealers;
	}

	private List<Vehicle> getVehicles(List<Integer> vehicleIds,RestTemplate restTemplate, String dataSetId) {
		List<Vehicle> vehicles = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		try {


			List<Future<Vehicle>> futures = null;
			List<Callable<Vehicle>> callables = new ArrayList<>();
			for (Integer vehicleId : vehicleIds) {
				Callable<Vehicle> callable = () -> restTemplate.getForObject(
								HOST_URL + dataSetId + "/vehicles/" + vehicleId, Vehicle.class);
				callables.add(callable);
			}

			futures = executorService.invokeAll(callables);

			for (Future<Vehicle> future : futures) {
				vehicles.add(future.get());
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		finally {
			executorService.shutdown();;
		}
		return vehicles;
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {

		return args -> {
			//  Get dataset od
			DataSet dataSet = restTemplate.getForObject(
					HOST_URL+"datasetId", DataSet.class);
		//	Get all the vehicle ids
			VehiclesForDataSet vehicleIds= restTemplate.getForObject(
					HOST_URL+dataSet.getDatasetId()+"/vehicles", VehiclesForDataSet.class);
			// Get all vehicles using ExecuutorService
			List<Vehicle> vehiclesForDataSet = getVehicles(vehicleIds.getVehicleIds(),restTemplate,dataSet.getDatasetId());
			// Get all dealers using ExecuutorService
			List<Dealer> dealerForDataSet = getAllDealers(vehiclesForDataSet,restTemplate,dataSet.getDatasetId());

			// Add vehiles to dealer objects
			for (Vehicle vehicle : vehiclesForDataSet) {
				Dealer dealer= dealerForDataSet.stream().filter(d -> d.getDealerId().equals(vehicle.getDealerId())).findFirst().orElse(null);
				if (dealer != null)
					dealer.getVehicles().add(vehicle);

			}
	/*		for (Dealer dealer : dealerForDataSet) {
				for (Vehicle vehicle: dealer.getVehicles())
					log.info("Dealer:"+ dealer.getDealerId()+"-Vehicle"+vehicle.getVehicleId());
			}
			*/
			DataSetDealers allDealers = new DataSetDealers();
			allDealers.setDealers(dealerForDataSet);
			HttpHeaders headers = new HttpHeaders();

			headers.setContentType(MediaType.APPLICATION_JSON);

			String json = new ObjectMapper().writeValueAsString(allDealers);
			//log.info(json);
			MultiValueMap<String, DataSetDealers> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add("dealers", allDealers);
			//HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity(bodyMap, headers);
			HttpEntity< String> requestEntity = new HttpEntity(json, headers);

			// Make final call to answer
			ResponseEntity<String> result = restTemplate.postForEntity(HOST_URL + dataSet.getDatasetId()+"/answer", requestEntity, String.class);
			log.info("*** Final result for answer call"+result.getBody());
		};
	}

}

