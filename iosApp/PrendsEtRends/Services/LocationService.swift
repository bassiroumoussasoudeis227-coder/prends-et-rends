import Foundation
import CoreLocation
import Combine

public final class LocationService: NSObject, ObservableObject, CLLocationManagerDelegate {
    public static let shared = LocationService()
    
    private let locationManager = CLLocationManager()
    private let geocoder = CLGeocoder()
    
    @Published public var currentLocation: CLLocation?
    @Published public var currentAddress: String?
    @Published public var isUpdating: Bool = false
    
    private var locationContinuation: CheckedContinuation<(CLLocation, String?), Error>?
    
    public override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
    }
    
    public func requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    public func captureLocation() async throws -> (CLLocation, String?) {
        let status = locationManager.authorizationStatus
        if status == .notDetermined {
            locationManager.requestWhenInUseAuthorization()
        }
        
        return try await withCheckedThrowingContinuation { continuation in
            self.locationContinuation = continuation
            self.isUpdating = true
            self.locationManager.requestLocation()
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        self.currentLocation = loc
        self.isUpdating = false
        
        geocoder.reverseGeocodeLocation(loc) { [weak self] placemarks, _ in
            var formattedAddr: String? = nil
            if let mark = placemarks?.first {
                let street = mark.thoroughfare ?? mark.name ?? ""
                let city = mark.locality ?? ""
                let country = mark.country ?? ""
                let parts = [street, city, country].filter { !$0.isEmpty }
                formattedAddr = parts.joined(separator: ", ")
            }
            self?.currentAddress = formattedAddr
            
            self?.locationContinuation?.resume(returning: (loc, formattedAddr))
            self?.locationContinuation = nil
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        self.isUpdating = false
        self.locationContinuation?.resume(throwing: error)
        self.locationContinuation = nil
    }
}
