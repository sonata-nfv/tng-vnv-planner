package com.github.tng.vnv.planner.data.repository

interface PackageRepository {

    def getRawPackageMetadata(String packageId)

}