package org.gateway;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class FutureTest {
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		long l = System.currentTimeMillis();
		List<Shop> list = Arrays.asList(new Shop("aaa"), new Shop("bbb"), new Shop("ccc"));
//		List<String> result = list.parallelStream()
//				.map(temp -> String.format("%s 的价格是 %.2f", temp.getName(), temp.getPrice("java8")))
//				.collect(Collectors.toList());

//		List<String> result1 = list.stream()
//				.map(temp -> String.format("%s 的价格是 %.2f", temp.getName(), temp.getPrice("java8")))
//				.collect(Collectors.toList());

//		List<CompletableFuture<String>> priceFuture = list.stream()
//				.map(shop -> CompletableFuture.supplyAsync(() -> shop.getName() + " 的价格是" + shop.getPrice("java8"),executorService))
//				.collect(Collectors.toList());
//

//		List<CompletableFuture<String>> priceFuture = list.stream()
//				.map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice2("java8实战"), executorService))
//				.map(future -> future.thenApply(Quote::parse))
//				.map(future -> future.thenCompose(
//						quote -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote), executorService)))
//				.collect(Collectors.toList());

		List<CompletableFuture<Double>> priceFuture = list.stream()
				.map(shop -> CompletableFuture.supplyAsync(() -> shop.getPrice("java8实战"), executorService))
				.map(future -> future.thenCombine(
						CompletableFuture.supplyAsync(() -> Discount.getRate(), executorService),
						(price, rate) -> price * rate))
				.collect(Collectors.toList());

		List<Double> result3 = priceFuture.stream().map(CompletableFuture::join).collect(Collectors.toList());
		for (Double temp : result3) {
			System.out.println(temp);
		}
		System.out.println(System.currentTimeMillis() - l);

	}

	static class Shop {
		private String name;
		private Random random = new Random();

		public Shop(String name) {
			// TODO Auto-generated constructor stub
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public double getPrice(String product) {
			return calculatePrice(product);
		}

		public String getPrice2(String product) {
			double price = calculatePrice(product);
			Discount.Code code = Discount.Code.values()[random.nextInt(Discount.Code.values().length)];
			System.out.println(name + ":" + price + ":" + code);
			return name + ":" + price + ":" + code;

		}

		public void delay() {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private double calculatePrice(String product) {
			delay();
			return random.nextDouble() * product.charAt(0) + product.charAt(1);
		}

		public Future<Double> getPriceAsync(String product) {
			CompletableFuture<Double> future = new CompletableFuture<>();

			new Thread(() -> {
				double price = calculatePrice(product);
				future.complete(price);
			});
			return future;
		}
	}

	static class Quote {
		private final String shopName;
		private final double price;
		private final Discount.Code code;

		public Quote(String shopName, double price, Discount.Code code) {
			this.shopName = shopName;
			this.price = price;
			this.code = code;
		}

		public String getShopName() {
			return shopName;
		}

		public double getPrice() {
			return price;
		}

		public Discount.Code getCode() {
			return code;
		}

		public static Quote parse(String s) {
			String[] arr = s.split(":");
			return new Quote(arr[0], Double.valueOf(arr[1]), Discount.Code.valueOf(arr[2]));
		}
	}

	static class Discount {
		public enum Code {
			NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

			private final int percentage;

			Code(int percentage) {
				this.percentage = percentage;
			}
		}

		public static String applyDiscount(Quote quote) {
			return quote.getShopName() + " price is " + Discount.apply(quote.getPrice(), quote.getCode());
		}

		public static double getRate() {
			// TODO Auto-generated method stub
			return 1;
		}

		public static double apply(double price, Code code) {
			delay();
			return price * (100 - code.percentage) / 100;
		}

		public static void delay() {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
